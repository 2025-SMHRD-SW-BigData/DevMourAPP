const mysql = require('mysql2/promise');

const dbConfig = {
    host: 'project-db-campus.smhrd.com',
    port: 3307,
    user: 'campus_25SW_BD_p3_2',
    password: 'smhrd2',
    database: 'campus_25SW_BD_p3_2'
};

async function checkAllMarkers() {
    const connection = await mysql.createConnection(dbConfig);
    
    try {
        console.log('데이터베이스 연결 성공!');
        
        // 전체 마커 수 확인
        const [countResult] = await connection.execute('SELECT COUNT(*) as total FROM t_markers');
        console.log(`\n총 마커 수: ${countResult[0].total}개`);
        
        // 모든 마커 데이터 조회
        const [rows] = await connection.execute('SELECT * FROM t_markers ORDER BY marker_id');
        
        console.log('\n=== 모든 마커 데이터 ===');
        rows.forEach((marker, index) => {
            console.log(`${index + 1}. 마커 ID: ${marker.marker_id}`);
            console.log(`   위도(lat): ${marker.lat}`);
            console.log(`   경도(lon): ${marker.lon}`);
            console.log(`   마커 타입: ${marker.marker_type}`);
            console.log(`   제어 인덱스: ${marker.control_idx}`);
            console.log(`   CCTV 인덱스: ${marker.cctv_idx}`);
            console.log('----------------------------------------');
        });
        
        // 위도/경도가 null인 마커 확인
        const [nullMarkers] = await connection.execute('SELECT * FROM t_markers WHERE lat IS NULL OR lon IS NULL');
        if (nullMarkers.length > 0) {
            console.log(`\n⚠️  위도/경도가 NULL인 마커: ${nullMarkers.length}개`);
            nullMarkers.forEach(marker => {
                console.log(`   마커 ID: ${marker.marker_id}, 타입: ${marker.marker_type}`);
            });
        }
        
    } catch (error) {
        console.error('데이터베이스 조회 실패:', error);
    } finally {
        await connection.end();
    }
}

// 스크립트 실행
checkAllMarkers();
