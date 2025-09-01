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

// 도로 목록 조회 - t_road 테이블에서 데이터 조회
router.get('/', async (req, res) => {
    try {
        // 데이터베이스 연결
        const connection = await mysql.createConnection(dbConfig);
        
        // t_road 테이블에서 모든 도로 데이터 조회
        const [rows] = await connection.execute(`
            SELECT 
                road_idx,
                anomaly_type,
                severity_level,
                detected_at,
                lat,
                lon,
                cctv_idx,
                img_file,
                v_idx,
                is_check,
                admin_id,
                check_date,
                is_resolved,
                resolved_at,
                by_citizen
            FROM t_road
            ORDER BY road_idx
        `);
        
        await connection.end();
        
        // 응답 데이터 형식에 맞게 변환
        const roads = rows.map(row => ({
            road_idx: row.road_idx,
            anomaly_type: row.anomaly_type,
            severity_level: row.severity_level,
            detected_at: row.detected_at,
            lat: parseFloat(row.lat),
            lon: parseFloat(row.lon),
            cctv_idx: row.cctv_idx,
            img_file: row.img_file,
            v_idx: row.v_idx,
            is_check: row.is_check,
            admin_id: row.admin_id,
            check_date: row.check_date,
            is_resolved: row.is_resolved,
            resolved_at: row.resolved_at,
            by_citizen: row.by_citizen
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

// 심각도별 도로 조회
router.get('/severity/:severity', async (req, res) => {
    try {
        const severityLevel = req.params.severity;
        
        // 데이터베이스 연결
        const connection = await mysql.createConnection(dbConfig);
        
        // 특정 심각도의 도로만 조회
        const [rows] = await connection.execute(`
            SELECT 
                road_idx,
                anomaly_type,
                severity_level,
                detected_at,
                lat,
                lon,
                cctv_idx,
                img_file,
                v_idx,
                is_check,
                admin_id,
                check_date,
                is_resolved,
                resolved_at,
                by_citizen
            FROM t_road
            WHERE severity_level = ?
            ORDER BY road_idx
        `, [severityLevel]);
        
        await connection.end();
        
        // 응답 데이터 형식에 맞게 변환
        const roads = rows.map(row => ({
            road_idx: row.road_idx,
            anomaly_type: row.anomaly_type,
            severity_level: row.severity_level,
            detected_at: row.detected_at,
            lat: parseFloat(row.lat),
            lon: parseFloat(row.lon),
            cctv_idx: row.cctv_idx,
            img_file: row.img_file,
            v_idx: row.v_idx,
            is_check: row.is_check,
            admin_id: row.admin_id,
            check_date: row.check_date,
            is_resolved: row.is_resolved,
            resolved_at: row.resolved_at,
            by_citizen: row.by_citizen
        }));
        
        res.json({
            success: true,
            message: `${severityLevel} 심각도 도로 데이터 조회 성공`,
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
