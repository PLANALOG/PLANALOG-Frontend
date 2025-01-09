<p align='center'>
    <img src="https://capsule-render.vercel.app/api?type=waving&color=2C3E50&height=300&section=header&text=PLANALOG&fontSize=70&animation=fadeIn&fontColor=ECF0F1&fontAlignY=38&desc=Frontend%20Repository&descAlignY=51&descAlign=62"/>
</p>

## 🔨Tech Stack
### Languages
- ![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?logo=kotlin&logoColor=white) 안드로이드 애플리케이션 개발의 주요 언어
### Frameworks & Libraries
- ![Android Jetpack](https://img.shields.io/badge/Jetpack-4285F4?logo=android&logoColor=white) LiveData, ViewModel, Navigation, Room 등
- ![Retrofit](https://img.shields.io/badge/Retrofit-00796B?logo=retrofit&logoColor=white) REST API 연동
- ![Glide](https://img.shields.io/badge/Glide-4285F4?logo=glide&logoColor=white) 이미지 로딩 및 캐싱
- ![Material Design](https://img.shields.io/badge/Material%20Design-757575?logo=material-design&logoColor=white) 안드로이드 UI 디자인
- ![View Binding](https://img.shields.io/badge/View%20Binding-8BC34A?logo=android&logoColor=white) 레이아웃 연결  
- ![Data Binding](https://img.shields.io/badge/Data%20Binding-673AB7?logo=android&logoColor=white) 데이터 연결  
- ![Coroutines](https://img.shields.io/badge/Coroutines-FF6F00?logo=kotlin&logoColor=white) 비동기 작업 처리  
- ![Kakao Login](https://img.shields.io/badge/Kakao%20Login-FEE500?logo=kakao&logoColor=black) 소셜 로그인 
- ![Firebase Cloud Messaging](https://img.shields.io/badge/FCM-FFCA28?logo=firebase&logoColor=white) 알림 기능 구현  
- ![CalendarView](https://img.shields.io/badge/CalendarView-009688?logo=android&logoColor=white) 캘린더 UI 및 데이터 연동  
- ![SearchView](https://img.shields.io/badge/SearchView-03A9F4?logo=android&logoColor=white) 검색 기능  
### Tools
- ![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?logo=android-studio&logoColor=white) 통합 개발 환경(IDE)  
- ![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white) 빌드 관리 도구  
### Version Control
- ![Git](https://img.shields.io/badge/Git-F05032?logo=git&logoColor=white) 코드 버전 관리  
- ![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white) 리포지토리 호스팅 및 협업  
### 주요 사용 기능
- ![FAB](https://img.shields.io/badge/FAB-6200EA?logo=material-design&logoColor=white) 게시글 작성 버튼  
- ![EditText](https://img.shields.io/badge/EditText-FF9800?logo=android&logoColor=white) 사용자 입력 처리
- ![TextInputEditText](https://img.shields.io/badge/TextInputEditText-FF9800?logo=android&logoColor=white) 텍스트 입력  
- ![TextCounter](https://img.shields.io/badge/TextCounter-8E24AA?logo=android&logoColor=white) 글자 수 제한 표시 (500자)  
- ![ActionBar](https://img.shields.io/badge/ActionBar-3F51B5?logo=android&logoColor=white) 작성 완료 버튼  
- ![Photo Picker API](https://img.shields.io/badge/Photo%20Picker%20API-009688?logo=android&logoColor=white) 사진 선택  
- ![ImageView](https://img.shields.io/badge/ImageView-03A9F4?logo=android&logoColor=white) 사진 미리보기  
- ![RichTextEditor](https://img.shields.io/badge/RichTextEditor-FF5722?logo=android&logoColor=white) 텍스트 입력 지원  
- ![ConstraintLayout](https://img.shields.io/badge/ConstraintLayout-3DDC84?logo=android&logoColor=white) UI 배치  
- ![LiveData](https://img.shields.io/badge/LiveData-43A047?logo=android&logoColor=white) 데이터 업데이트 반영  
- ![Room](https://img.shields.io/badge/Room-FF6F00?logo=android&logoColor=white) 데이터 저장  
  <br/><br/>

## Convention

> Branch Convention
- **배포용**: main
- **개발용**: develop
- **작업용**: 커밋 유형/#이슈번호-설명 (feature/#3-로그인레이아웃)
  <br/><br/>
> Coding Convention
* **`Class & Interface`** : **Upper Camel Case**
  ex) LoginActivity, PokeService
- **`Composable 함수`** : **Upper Camel Case**
  ex) OnboardingPage, OnboardingPageTitle
- **`함수와 변수`** : **Lower Camel Case**

- **initXXX()** : 초기화 함수 이름
    - **init[View]ClickListener** : 클릭 리스너 설정
    - **init[NameView]Adapter** : 리사이클러뷰 어댑터 설정
      ```
      fun **initPresentAdapter**(){
      binding.nameRv.adapter = PresentAdapter()
      }
      ```
    - **updateXXX()** : 갱신 함수 이름
    - **removeXXX()** : 삭제 함수 이름
    - **setupXXX()** : ViewModel을 observe()할 때 모아놓는 함수 이름
    - **getXXX()** : Return이 있는 데이터를 불러올때 함수 이름
    - **findXXX()** : 특정 객체를 찾는 함수 이름
    - **복수형을 가져올때는 뒤에 s를 붙인다:** getBrands() 꼴
    - **Raw 값으로부터 enum을 찾을 때 함수 이름은 `find()`로 한다.**
    - 서버 통신 함수
        - **getXXX()** → getUserList()
        - **deleteXXX()** → deleteUser()
        - **putXXX()** → putProfile()
        - **postXXX()** → postMusic()
          <br/><br/>
> Issue Convention
- 양식 | [커밋 유형(전부 대문자)] 이슈 내용
- 예시 | [FEAT] 추억 등록하기 UI 구현
  <br/><br/>

> PR Convention
- 양식
    1. PR 타입
        * 기능 추가
        * 기능 삭제
        * 버그 수정
        * 의존성, 환경 변수, 빌드 관련 코드 업데이트
    2. 반영 브랜치
       ex) feat/#9-카카오로그인구현 -> develop
    3. 변경 사항
       [변경 내용 작성]
    4. 테스트 결과
       [구현 화면 첨부]
       <br/><br/>
  
> Commit Convention
1. 커밋 유형 : 첫 글자만 영어 대문자로 작성
    - Feat : 새로운 기능 추가
    - Fix : 버그 수정
    - Remove : 파일, 코드 삭제 또는 기능 삭제
    - Refactor : 코드 리팩토링
    - Style : 코드 스타일 수정
    - Test : 테스트 코드 추가/수정

2. 커밋 메세지
    - 양식 | 커밋 유형 : 간단한 코드 설명 (#이슈 번호)
    - 예시 | git commit -m "Feat : 카카오 로그인 기능 구현 (#9)

3. 그 외 규칙
    - 제목과 본문을 빈행으로 분리
        - 커밋 유형 이후 제목과 본문은 한글로 작성하여 내용이 잘 전달될 수 있도록 할 것
        - 본문에는 변경한 내용과 이유 설명 (어떻게보다는 무엇 & 왜를 설명)
    - 제목 첫 글자는 대문자로, 끝에는 `.` 금지
    - 제목은 영문 기준 50자 이내로 할 것
    - 자신의 코드가 직관적으로 바로 파악할 수 있다고 생각하지 말자
    - 여러가지 항목이 있다면 글머리 기호를 통해 가독성 높이기
      <br/><br/>
   
## 폴더 구조
📁 ProjectRoot/<br/>
│<br/>
├── 📁 app/                # 메인 애플리케이션 폴더<br/>
│   ├── 📁 src/            # 소스 코드 폴더<br/>
│   │   ├── 📁 main/       # 메인 소스 코드<br/>
│   │   │   ├── 📁 java/   # Java/Kotlin 코드<br/>
│   │   │   │   ├── 📁 com.example.app/  # 패키지 네임스페이스<br/>
│   │   │   │   │   ├── MainActivity.kt        # 메인 액티비티<br/>
│   │   │   │   │   ├── 📁 ui/                 # UI 관련 코드<br/>
│   │   │   │   │   ├── 📁 model/              # 데이터 모델<br/>
│   │   │   │   │   ├── 📁 network/              # API 관련<br/>
│   │   │   │   │   ├── 📁 repository/         # 데이터 관리<br/>
│   │   │   │   │   ├── 📁 utils/              # 유틸리티 클래스<br/>
│   │   │   ├── 📁 res/    # 리소스 폴더 (XML, 이미지 등)<br/>
│   │   │   │   ├── 📁 layout/   # 레이아웃<br/>
│   │   │   │   ├── 📁 font/ # 폰트<br/>
│   │   │   │   ├── 📁 drawable/               # 이미지, 아이콘<br/>
│   │   │   │   ├── 📁 values/                 # 스타일, 문자열, 컬러<br/>
│   │   │   │   │   ├── colors.xml<br/>
│   │   │   │   │   ├── strings.xml<br/>
│   │   │   │   │   └── themes.xml<br/>
│   │   │   └── AndroidManifest.xml # 매니페스트 파일<br/>
│   │<br/>
│   └── 📁 test/            # 테스트 코드 (필요하면 추가)<br/>
│<br/>
└── 📁 gradle/              # Gradle 관련 설정<br/>
<br/>

> 그 외 설정
- Android Studio 버전: Koala
- target SDK: 34
- minSDK: 29
- 테스트 형식: 실제 디바이스