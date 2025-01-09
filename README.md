<p align='center'>
    <img src="https://capsule-render.vercel.app/api?type=waving&color=2C3E50&height=300&section=header&text=PLANALOG&fontSize=70&animation=fadeIn&fontColor=ECF0F1&fontAlignY=38&desc=Frontend%20Repository&descAlignY=51&descAlign=62"/>
</p>

## 🔨Tech Stack
- Kotlin : 주 사용 언어
- View Binding : 레이아웃 연결
- Data Binding :  데이터 연결
- Coroutine : 비동기 작업 중 상태 업데이트
- EditText : 사용자 입력 처리
- Retrofit2 : API 연결
- Social Login : Kakao 로그인
- Other Tools | Notion, Figma
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