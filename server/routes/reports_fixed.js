const express = require('express');
const mysql = require('mysql2/promise');
const multer = require('multer');
const path = require('path');
const fs = require('fs').promises;
const axios = require('axios');

const router = express.Router();

// multer 설정 - 이미지 파일 업로드용
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        const uploadDir = path.join(__dirname, '../uploads');
        // uploads 디렉토리가 없으면 생성
        fs.mkdir(uploadDir, { recursive: true }).then(() => {
            cb(null, uploadDir);
        }).catch(err => {
            cb(err);
        });
    },
    filename: function (req, file, cb) {
        // 파일명: timestamp_originalname
        const uniqueSuffix = Date.now() + '_' + Math.round(Math.random() * 1E9);
        cb(null, uniqueSuffix + path.extname(file.originalname));
    }
});

const upload = multer({ 
    storage: storage,
    limits: {
        fileSize: 5 * 1024 * 1024 // 5MB 제한
    },
    fileFilter: function (req, file, cb) {
        // 이미지 파일만 허용
        if (file.mimetype.startsWith('image/')) {
            cb(null, true);
        } else {
            cb(new Error('이미지 파일만 업로드 가능합니다.'), false);
        }
    }
});

// MySQL Pool 설정
const db = mysql.createPool({
    host: 'project-db-campus.smhrd.com',
    port: 3307,
    user: 'campus_25SW_BD_p3_2',
    password: 'smhrd2',
    database: 'campus_25SW_BD_p3_2',
    charset: 'utf8mb4',
    waitForConnections: true,
    connectionLimit: 10
});

// 민원 제출 API
router.post('/submit', upload.fields([
    { name: 'c_report_file1', maxCount: 1 },
    { name: 'c_report_file2', maxCount: 1 },
    { name: 'c_report_file3', maxCount: 1 }
]), async (req, res) => {
    try {
        // 요청 데이터 파싱
        const {
            addr = null, // 주소는 null로 설정
            c_report_detail = '',
            lat = null,
            lon = null,
            c_reporter_name = null,
            c_reporter_phone = null
        } = req.body;
        
        console.log('받은 데이터:', {
            addr,
            c_report_detail,
            lat,
            lon,
            c_reporter_name,
            c_reporter_phone
        });
        
        // 필수 필드 검증 (주소는 null 허용)
        if (!c_report_detail) {
            return res.status(400).json({
                success: false,
                message: '제보 내용은 필수 입력 항목입니다.'
            });
        }
        
        // 클라이언트에서 전송된 이미지 URL 처리
        const files = req.files;
        const fileUrls = {
            c_report_file1: null,
            c_report_file2: null,
            c_report_file3: null
        };
        
        // 클라이언트에서 이미 S3에 업로드된 URL을 받아서 처리
        if (files.c_report_file1 && files.c_report_file1[0]) {
            // 클라이언트에서 전송된 데이터가 URL인지 확인
            const fileData = files.c_report_file1[0];
            if (fileData.buffer && fileData.buffer.toString().startsWith('http')) {
                // URL인 경우
                fileUrls.c_report_file1 = fileData.buffer.toString();
                console.log('클라이언트에서 전송된 이미지 URL:', fileUrls.c_report_file1);
            } else {
                // 파일인 경우 (기존 방식) - 파일명만 저장
                fileUrls.c_report_file1 = fileData.filename;
                console.log('로컬 파일명 저장:', fileUrls.c_report_file1);
            }
        }
        
        if (files.c_report_file2 && files.c_report_file2[0]) {
            const fileData = files.c_report_file2[0];
            if (fileData.buffer && fileData.buffer.toString().startsWith('http')) {
                fileUrls.c_report_file2 = fileData.buffer.toString();
            } else {
                fileUrls.c_report_file2 = fileData.filename;
            }
        }
        
        if (files.c_report_file3 && files.c_report_file3[0]) {
            const fileData = files.c_report_file3[0];
            if (fileData.buffer && fileData.buffer.toString().startsWith('http')) {
                fileUrls.c_report_file3 = fileData.buffer.toString();
            } else {
                fileUrls.c_report_file3 = fileData.filename;
            }
        }
        
        // 지오코딩을 위한 주소 변환
        let geocodedAddr = null;
        try {
            const geocodingResponse = await axios.get(`http://localhost:3001/api/weather/reverse?lat=${lat}&lon=${lon}`);
            
            if (geocodingResponse.data && geocodingResponse.data.success) {
                geocodedAddr = geocodingResponse.data.data.address || geocodingResponse.data.data.formatted_address;
                console.log('역 지오코딩 성공:', geocodedAddr);
            } else {
                console.log('역 지오코딩 실패, 기본값 사용');
            }
        } catch (geocodingError) {
            console.error('역 지오코딩 오류:', geocodingError.message);
            console.log('역 지오코딩 실패, 기본값 사용');
        }
        
        // DB에 민원 데이터 저장
        const insertQuery = `
            INSERT INTO t_citizen_report (
                c_reported_at,
                lat,
                lon,
                c_report_detail,
                c_report_file1,
                c_report_file2,
                c_report_file3,
                c_reporter_name,
                c_reporter_phone,
                c_report_status,
                admin_id,
                addr
            ) VALUES (NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `;
        
        const [result] = await db.execute(insertQuery, [
            lat, // lat (위도) - 클라이언트에서 전송된 값
            lon, // lon (경도) - 클라이언트에서 전송된 값
            c_report_detail, // 카테고리 정보 (도로침수/도로빙결)
            fileUrls.c_report_file1, // 첫 번째 사진 S3 URL 또는 파일명
            fileUrls.c_report_file2, // 두 번째 사진 S3 URL 또는 파일명
            fileUrls.c_report_file3, // 세 번째 사진 S3 URL 또는 파일명
            null, // c_reporter_name (제보자 성명) - 아직 연결 안됨
            null, // c_reporter_phone (제보자 연락처) - 아직 연결 안됨
            'P', // c_report_status (처리 상태)
            null, // admin_id (관리자 ID) - 아직 연결 안됨
            geocodedAddr // addr (지오코딩된 주소)
        ]);
        
        console.log('민원 제출 성공:', {
            reportId: result.insertId,
            addr: geocodedAddr,
            lat,
            lon,
            c_report_detail,
            files: fileUrls
        });

        // SSE를 위한 실시간 알림 전송
        try {
            const { broadcastNotification } = require('./notifications');
            const notificationMessage = `${geocodedAddr || '위치 정보 없음'} 지역에서 ${c_report_detail} 민원 접수!`;
            
            broadcastNotification({
                type: 'citizen_report',
                message: notificationMessage,
                reportId: result.insertId,
                addr: geocodedAddr,
                c_report_detail,
                lat,
                lon,
                timestamp: new Date().toISOString()
            });
            
            console.log('SSE 알림 전송 완료:', notificationMessage);
        } catch (sseError) {
            console.error('SSE 알림 전송 실패:', sseError.message);
        }
        
        res.json({
            success: true,
            message: '민원이 성공적으로 제출되었습니다.',
            data: {
                reportId: result.insertId,
                addr: geocodedAddr,
                lat,
                lon,
                c_report_detail,
                files: fileUrls
            }
        });
        
    } catch (error) {
        console.error('민원 제출 오류:', error);
        
        // 업로드된 파일들 정리 (오류 시)
        if (req.files) {
            for (const fieldName in req.files) {
                for (const file of req.files[fieldName]) {
                    try {
                        await fs.unlink(file.path);
                    } catch (unlinkError) {
                        console.error('파일 삭제 오류:', unlinkError);
                    }
                }
            }
        }
        
        res.status(500).json({
            success: false,
            message: '민원 제출 중 오류가 발생했습니다.',
            error: error.message
        });
    }
});

// 민원 목록 조회 API
router.get('/list', async (req, res) => {
    try {
        const query = `
            SELECT 
                c_report_idx,
                addr,
                c_report_detail,
                lat,
                lon,
                c_report_file1,
                c_report_file2,
                c_report_file3,
                c_reporter_name,
                c_reporter_phone,
                c_report_status,
                admin_id,
                c_reported_at
            FROM t_citizen_report 
            ORDER BY c_reported_at DESC
        `;
        
        const [rows] = await db.execute(query);
        
        res.json({
            success: true,
            data: rows
        });
        
    } catch (error) {
        console.error('민원 목록 조회 오류:', error);
        res.status(500).json({
            success: false,
            message: '민원 목록 조회 중 오류가 발생했습니다.',
            error: error.message
        });
    }
});

module.exports = router;
