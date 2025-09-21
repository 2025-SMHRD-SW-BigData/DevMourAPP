const express = require('express');
const mysql = require('mysql2/promise');
const multer = require('multer');
const path = require('path');
const fs = require('fs').promises;
const { uploadToS3, deleteFromS3 } = require('../config/s3Config');

const router = express.Router();

// multer 설정 - 메모리 스토리지 (S3 업로드용)
const storage = multer.memoryStorage();

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
                // 파일인 경우 (기존 방식)
                const uploadResult = await uploadToS3(fileData);
                if (uploadResult.success) {
                    fileUrls.c_report_file1 = uploadResult.url;
                } else {
                    throw new Error(`첫 번째 이미지 업로드 실패: ${uploadResult.error}`);
                }
            }
        }
        
        if (files.c_report_file2 && files.c_report_file2[0]) {
            const fileData = files.c_report_file2[0];
            if (fileData.buffer && fileData.buffer.toString().startsWith('http')) {
                fileUrls.c_report_file2 = fileData.buffer.toString();
            } else {
                const uploadResult = await uploadToS3(fileData);
                if (uploadResult.success) {
                    fileUrls.c_report_file2 = uploadResult.url;
                } else {
                    throw new Error(`두 번째 이미지 업로드 실패: ${uploadResult.error}`);
                }
            }
        }
        
        if (files.c_report_file3 && files.c_report_file3[0]) {
            const fileData = files.c_report_file3[0];
            if (fileData.buffer && fileData.buffer.toString().startsWith('http')) {
                fileUrls.c_report_file3 = fileData.buffer.toString();
            } else {
                const uploadResult = await uploadToS3(fileData);
                if (uploadResult.success) {
                    fileUrls.c_report_file3 = uploadResult.url;
                } else {
                    throw new Error(`세 번째 이미지 업로드 실패: ${uploadResult.error}`);
                }
            }
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
            fileUrls.c_report_file1, // 첫 번째 사진 S3 URL
            fileUrls.c_report_file2, // 두 번째 사진 S3 URL
            fileUrls.c_report_file3, // 세 번째 사진 S3 URL
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
            files: fileUrls
        });
        
        res.json({
            success: true,
            message: '민원이 성공적으로 제출되었습니다.',
            data: {
                reportId: result.insertId,
                addr,
                c_report_detail,
                files: fileUrls
            }
        });
        
    } catch (error) {
        console.error('민원 제출 오류:', error);
        
        // S3에 업로드된 파일들 정리 (오류 시)
        // 이미 업로드된 파일들의 URL을 추적하여 삭제
        // 실제 구현에서는 업로드된 파일들의 키를 추적해야 함
        
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

// 이미지 파일 제공 API (S3 URL을 직접 사용하므로 제거)
// 클라이언트에서는 S3 URL을 직접 사용하여 이미지를 표시

module.exports = router;
