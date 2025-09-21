# AWS S3 환경 변수 설정 가이드

## ⚠️ 보안 주의사항
이 프로젝트에서 하드코딩된 AWS 자격 증명을 제거했습니다. 
**절대로 AWS 자격 증명을 코드에 직접 작성하지 마세요.**

## 서버 설정

### 1. 환경 변수 파일 생성
```bash
# server 폴더에서
cp env.example .env
```

### 2. .env 파일 편집
```bash
# 실제 AWS 자격 증명으로 교체
AWS_ACCESS_KEY_ID=your_actual_access_key_id
AWS_SECRET_ACCESS_KEY=your_actual_secret_access_key
AWS_REGION=ap-southeast-2
AWS_S3_BUCKET_NAME=devmour
```

### 3. 환경 변수 로드 (dotenv 사용)
서버 시작 전에 환경 변수를 로드하도록 server.js를 수정하세요:
```javascript
require('dotenv').config();
```

## Android 앱 설정

### 방법 1: 시스템 환경 변수 설정
```bash
# Windows
set AWS_ACCESS_KEY_ID=your_actual_access_key_id
set AWS_SECRET_ACCESS_KEY=your_actual_secret_access_key

# macOS/Linux
export AWS_ACCESS_KEY_ID=your_actual_access_key_id
export AWS_SECRET_ACCESS_KEY=your_actual_secret_access_key
```

### 방법 2: Android Studio 환경 변수 설정
1. Android Studio에서 Run Configuration 열기
2. Environment Variables 섹션에서 다음 추가:
   - `AWS_ACCESS_KEY_ID`: your_actual_access_key_id
   - `AWS_SECRET_ACCESS_KEY`: your_actual_secret_access_key

## AWS S3 버킷 설정

### 1. CORS 설정
S3 버킷의 CORS 설정에 다음 정책 추가:
```json
[
    {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
        "AllowedOrigins": ["*"],
        "ExposeHeaders": []
    }
]
```

### 2. 버킷 정책
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::devmour/*"
        }
    ]
}
```

## 문제 해결

### 환경 변수가 로드되지 않는 경우
1. 서버 재시작
2. Android Studio 재시작
3. 환경 변수 파일 경로 확인
4. 파일 권한 확인

### AWS 자격 증명 오류
1. AWS 콘솔에서 IAM 사용자 권한 확인
2. S3 버킷 접근 권한 확인
3. 리전 설정 확인

## 보안 체크리스트
- [ ] 하드코딩된 자격 증명 제거됨
- [ ] .env 파일이 .gitignore에 포함됨
- [ ] 실제 자격 증명이 공개 저장소에 없음
- [ ] 환경 변수가 올바르게 설정됨
