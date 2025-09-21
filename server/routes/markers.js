const express = require('express');
const router = express.Router();
const mysql = require('mysql2/promise');

// 데이터베이스 연결 설정
const dbConfig = {
    host: 'project-db-campus.smhrd.com',
    port: 3307,
    user: 'campus_25SW_BD_p3_2',
    password: 'smhrd2',
    database: 'campus_25SW_BD_p3_2'
};

// 도로 목록 조회 - t_total 테이블에서 데이터 조회
router.get('/', async (req, res) => {
    try {
        // 데이터베이스 연결
        const connection = await mysql.createConnection(dbConfig);
        
        // t_total 테이블에서 각 CCTV별 최신 데이터 조회
        const [rows] = await connection.execute(`
            SELECT total_idx, cctv_idx, lat, lon, total_score, detected_at
            FROM
                ( SELECT *,
                    ROW_NUMBER() OVER(PARTITION BY cctv_idx ORDER BY detected_at DESC) AS rn 
                  FROM t_total
                ) AS T
            WHERE T.rn = 1
        `);
        
        await connection.end();
        
        // 응답 데이터 형식에 맞게 변환
        const roads = rows.map(row => ({
            total_idx: row.total_idx,
            cctv_idx: row.cctv_idx,
            lat: parseFloat(row.lat),
            lon: parseFloat(row.lon),
            total_score: parseFloat(row.total_score),
            detected_at: row.detected_at
        }));
        
        res.json({
            success: true,
            message: '도로 데이터 조회 성공',
            data: roads
        });
        
    } catch (error) {
        console.error('도로 데이터 조회 오류:', error);
        res.status(500).json({
            success: false,
            message: '도로 데이터 조회에 실패했습니다',
            error: error.message
        });
    }
});

// 등급별 도로 조회
router.get('/grade/:grade', async (req, res) => {
    try {
        const grade = req.params.grade;
        
        // 데이터베이스 연결
        const connection = await mysql.createConnection(dbConfig);
        
        // 특정 등급의 도로만 조회
        let scoreCondition;
        switch(grade) {
            case '안전':
                scoreCondition = 'total_score >= 0.0 AND total_score <= 4.0';
                break;
            case '경고':
                scoreCondition = 'total_score >= 4.1 AND total_score <= 7.0';
                break;
            case '위험':
                scoreCondition = 'total_score >= 7.1 AND total_score <= 10.0';
                break;
            default:
                scoreCondition = 'total_score >= 0.0 AND total_score <= 4.0';
        }
        
        const [rows] = await connection.execute(`
            SELECT total_idx, cctv_idx, lat, lon, total_score, detected_at
            FROM
                ( SELECT *,
                    ROW_NUMBER() OVER(PARTITION BY cctv_idx ORDER BY detected_at DESC) AS rn 
                  FROM t_total
                  WHERE ${scoreCondition}
                ) AS T
            WHERE T.rn = 1
        `);
        
        await connection.end();
        
        // 응답 데이터 형식에 맞게 변환
        const roads = rows.map(row => ({
            total_idx: row.total_idx,
            cctv_idx: row.cctv_idx,
            lat: parseFloat(row.lat),
            lon: parseFloat(row.lon),
            total_score: parseFloat(row.total_score),
            detected_at: row.detected_at
        }));
        
        res.json({
            success: true,
            message: `${grade} 등급 도로 데이터 조회 성공`,
            data: roads
        });
        
    } catch (error) {
        console.error('도로 데이터 조회 오류:', error);
        res.status(500).json({
            success: false,
            message: '도로 데이터 조회에 실패했습니다',
            error: error.message
        });
    }
});

module.exports = router;
