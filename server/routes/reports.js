const express = require('express');
const mysql = require('mysql2/promise');
const multer = require('multer');
const path = require('path');
const fs = require('fs').promises;

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

// DB 연결 설정
const dbConfig = {
    host: 'project-db-campus.smhrd.com',
    port: 3307,
    user: 'campus_25SW_BD_p3_2',
    password: 'smhrd2',
    database: 'campus_25SW_BD_p3_2', // 사용자명과 동일한 DB명으로 설정
    charset: 'utf8mb4'
};

// 민원 제출 API
router.post('/submit', upload.fields([
    { name: 'c_report_file1', maxCount: 1 },
    { name: 'c_report_file2', maxCount: 1 },
    { name: 'c_report_file3', maxCount: 1 }
]), async (req, res) => {
    let connection;
    
    try {
        // DB 연결
        connection = await mysql.createConnection(dbConfig);
        
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
        
        // 업로드된 파일 정보 수집
        const files = req.files;
        const filePaths = {
            c_report_file1: null,
            c_report_file2: null,
            c_report_file3: null
        };
        
        // 파일 경로 저장
        if (files.c_report_file1 && files.c_report_file1[0]) {
            filePaths.c_report_file1 = files.c_report_file1[0].filename;
        }
        if (files.c_report_file2 && files.c_report_file2[0]) {
            filePaths.c_report_file2 = files.c_report_file2[0].filename;
        }
        if (files.c_report_file3 && files.c_report_file3[0]) {
            filePaths.c_report_file3 = files.c_report_file3[0].filename;
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
        
        const [result] = await connection.execute(insertQuery, [
            lat, // lat (위도) - 클라이언트에서 전송된 값
            lon, // lon (경도) - 클라이언트에서 전송된 값
            c_report_detail, // 카테고리 정보 (도로침수/도로빙결/도로파손)
            filePaths.c_report_file1, // 첫 번째 사진
            filePaths.c_report_file2, // 두 번째 사진
            filePaths.c_report_file3, // 세 번째 사진
            null, // c_reporter_name (제보자 성명) - 아직 연결 안됨
            null, // c_reporter_phone (제보자 연락처) - 아직 연결 안됨
            'pending', // c_report_status (처리 상태)
            null, // admin_id (관리자 ID) - 아직 연결 안됨
            null // addr (주소) - 아직 연결 안됨
        ]);
        
        console.log('민원 제출 성공:', {
            reportId: result.insertId,
            addr,
            c_report_detail,
            files: filePaths
        });
        
        res.json({
            success: true,
            message: '민원이 성공적으로 제출되었습니다.',
            data: {
                reportId: result.insertId,
                addr,
                c_report_detail,
                files: filePaths
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
    } finally {
        if (connection) {
            await connection.end();
        }
    }
});

// 민원 목록 조회 API
router.get('/list', async (req, res) => {
    let connection;
    
    try {
        connection = await mysql.createConnection(dbConfig);
        
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
        
        const [rows] = await connection.execute(query);
        
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
    } finally {
        if (connection) {
            await connection.end();
        }
    }
});

// 이미지 파일 제공 API
router.get('/image/:filename', async (req, res) => {
    try {
        const filename = req.params.filename;
        const imagePath = path.join(__dirname, '../uploads', filename);
        
        // 파일 존재 확인
        await fs.access(imagePath);
        
        // 이미지 파일 전송
        res.sendFile(imagePath);
        
    } catch (error) {
        console.error('이미지 제공 오류:', error);
        res.status(404).json({
            success: false,
            message: '이미지를 찾을 수 없습니다.'
        });
    }
});

module.exports = router;
