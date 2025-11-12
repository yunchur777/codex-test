# Yeogiottae Nearby Lodging App

간단한 "여기어때" 스타일의 Jetpack Compose 샘플 애플리케이션입니다. 현재 위치 정보를 기반으로 주변 숙박업소 목록을 보여주고 예약 이벤트를 트리거할 수 있는 구조를 포함합니다. Compose, ViewModel, Hilt 기반 DI를 결합한 아키텍처 예시를 제공합니다.

## 구성
- **UI (Compose)**: `HomeScreen`은 로딩, 비어있는 상태, 숙소 리스트, 스낵바 메시지 표시를 담당합니다.
- **ViewModel**: `AccommodationViewModel`은 위치 스트림과 숙소 조회 UseCase를 결합해 상태를 관리합니다.
- **Domain**: `GetNearbyAccommodationsUseCase`는 리포지토리에서 숙소 리스트를 가져오는 역할을 수행합니다.
- **Data**: `AccommodationRepository`는 현재 Fake 구현(`FakeAccommodationRepository`)으로, 네트워크 호출 대신 임의의 데이터를 제공합니다.
- **Location**: `LocationProvider` 인터페이스와 `FusedLocationProvider` 구현이 위치 정보를 제공합니다.
- **DI**: `AppModule`에서 Repository 및 Location Provider 의존성을 Hilt로 주입합니다.

## 빌드 방법
1. Android Studio Giraffe 이상 또는 최신 버전에서 프로젝트를 엽니다.
2. Gradle Sync 후 Android 기기(또는 에뮬레이터)에 앱을 실행합니다.
3. 위치 권한을 허용하면 현재 위치를 기반으로 한 숙박 리스트를 확인할 수 있습니다.

> 실제 예약 기능은 데모 목적의 성공 스낵바만 표시합니다. 위치 권한 상태 감지는 샘플 수준이며, 실제 앱에서는 런타임 권한 요청 처리가 필요합니다.
