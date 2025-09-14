# DevMourAPP
일반 사용자들을 위해 도로시(SEE) 웹 대시보드의 값을 받아와 시민들에게 정보를 제공해주고 알림을 띄워주는 어플리케이션

# 프로젝트 소개
YOLOv8을 활용한 실시간 도로 손상 감지 및 위험도 평가 서비스 CCTV 영상과 AI를 활용하여 도로 파손을 실시간으로 감지하고, 도로 손상 유형을 자동 분류하여 종합적인 도로 상태 평가 시스템 구축하고 일반 시민 사용자들에게 지도기반 정보를 제공하고 민원신고, 실시간 알림 기능을 제공하기 위한 어플을 개발하고자 합니다.

# 개발 기간
2025.08.29~25.09.10

# 개발자 소개(역할분담 상세)
팀장 : 김상중 - 프로젝트 총괄 PM

팀원 : 전혜미 - 프로젝트 기획, UI 설계

팀원 : 박주진 - API 통합 관리, 민원신고 이미지 클라우드 스토리지 AWS S3 연동

팀원 : 박보라 - 앱 지도 API 활용 주변 위험 정보 가시화

팀원 : 최정운 - 앱 민원 신고 페이지 앱 UI/UX

팀원 : 송달상 - 앱 PUSH 알림 기능, 앱 소셜 로그인 기능, 앱 알림 내역 구현

# 개발환경
Android Studio

# 기술스택
Kotlin, AWS S3, Naver Maps

# 주요기능
+ 실시간 push 알림 기능
    + 관리자가 통제구역 추가시 실시간 push 알림 서비스 제공
+ 지도 기반 위험 정보 제공
    + 주변 300m내 위험 정보 존재시 현재위치에 빨간점으로 시각화
    + 마커 클릭시 상세 내용 확인 가능
+ 민원 신고 기능
    + 소셜 로그인으로 간편 로그인 가능
    + 클라우드 스토리지와 연동하여 제보 이미지 저장
    + 민원신고 완료시 웹 관리자 대시보드로 실시간 알림 발송
    + 제보된 이미지를 바탕으로 관리자가 AI 도로파손 분석
      
# 프로젝트 아키텍쳐

# 시연영상
youtube.com/watch?si=M73OB3VOOP5eas6v&v=o9kYfwejQRQ&feature=youtu.be

# 최종발표자료
https://www.canva.com/design/DAGyomBQRs0/4u21qiH1jrva4l-i-febYQ/edit?utm_content=DAGyomBQRs0&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton)
