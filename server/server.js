const express = require('express');
const cors = require('cors');
const path = require('path');
const markersRouter = require('./routes/markers');
const roadControlsRouter = require('./routes/road-controls');
const reportsRouter = require('./routes/reports');

const app = express();
const PORT = 3000; // 명시적으로 포트 3000 사용

// 미들웨어 설정
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 정적 파일 서빙 - 마커 아이콘 이미지들을 제공
app.use('/images', express.static(path.join(__dirname, '../app/src/main/res/drawable')));

// 요청 로깅 미들웨어
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
    next();
});

// 라우터 설정
app.use('/api/mobile/markers', markersRouter);
app.use('/api/mobile/road-controls', roadControlsRouter);
app.use('/api/mobile/reports', reportsRouter);

// 기본 라우트
app.get('/', (req, res) => {
    res.json({ 
        message: 'DevMour API 서버가 실행 중입니다.',
        timestamp: new Date().toISOString(),
        endpoints: {
            roads: '/api/roads',
            roadControls: '/api/road-controls',
            reports: '/api/reports'
        }
    });
});

// 헬스체크 엔드포인트
app.get('/health', (req, res) => {
    res.json({ 
        status: 'OK',
        timestamp: new Date().toISOString()
    });
});

// 서버 시작
app.listen(PORT, '0.0.0.0',() => {
    console.log(`=================================`);
    console.log(`서버가 포트 ${PORT}에서 실행 중입니다.`);
    console.log(`마커 API 엔드포인트: http://localhost:${PORT}/api/mobile/markers`);
    console.log(`도로 통제 API 엔드포인트: http://localhost:${PORT}/api/mobile/road-controls`);
    console.log(`민원 제출 API 엔드포인트: http://localhost:${PORT}/api/mobile/reports`);
    console.log(`헬스체크: http://localhost:${PORT}/health`);
    console.log(`=================================`);
});
