# 개인프로젝트 - 휴대폰 인증 시스템
___
## 기획 의도
![image](https://github.com/user-attachments/assets/b62c73a6-616e-4208-a36f-e5a5233260c8)

 휴대폰 인증 API들은 대부분 유료이고 무료인 부분도 처음 몇번을 빼고는 유료 API이기 때문에 접근이 쉽지 않았습니다.
그래서 여러가지 방법을 찾아보던 중 https://obtuse.kr/dev/free-phone-verification/ 이 블로그를 찾게 되었습니다.

![image](https://github.com/user-attachments/assets/fc042499-9deb-4d25-b57b-afa5bc73fe82)

 통신사의 MMS 기능을 이용하는 것으로 이메일로 받을 경우 통신사와 전화번호가 같이 뜬다는 점을 이용하여 휴대폰 인증을 구현 하는 방법이었습니다.
해당 블로그와는 사용 기술이 달랐기에 이 아이디어를 가지고 저의 기술에 맞게 적용해보았습니다.

___

## 진행 과정
### 필요 조건 확인
![image](https://github.com/user-attachments/assets/e3400931-ad8e-4f84-ad65-b535b45126bd)
 각 통신사 별로 ＭＭＳ에서 쓰는 이름이 달랐기에 조사하여 확인했습니다.

![image](https://github.com/user-attachments/assets/eefe4575-feb1-4fa9-8a31-a33709b9779b)
 구글 이메일을 활용하기 위해 POP/IMAP 서비스를 이용했습니다.
 
![image](https://github.com/user-attachments/assets/cee87d87-671f-4236-bdfd-694ef569fa3d)
 필요한 기능을 스프링에 추가하고 포트 번호와 앱비밀번호를 설정하였습니다.
 
### 코드 입력
![image](https://github.com/user-attachments/assets/cc4fa7c7-2609-48b9-99f5-54cebe216710)
![image](https://github.com/user-attachments/assets/7bd1364e-d10c-46f2-a2d3-9cc32dd08a49)
![image](https://github.com/user-attachments/assets/4d19ddc8-a080-4e5a-9e66-9a59a0708c66)
![image](https://github.com/user-attachments/assets/fe4c11df-730d-41ee-a560-daa13519c6c1)

## 기능 확인
![image](https://github.com/user-attachments/assets/5743eb3c-9a6d-4183-8a40-a20a4f5383ac)
![image](https://github.com/user-attachments/assets/aff75755-ca18-48b8-b0d3-c153109877a5)
![image](https://github.com/user-attachments/assets/e20403a4-8fcf-4d71-babd-da7364bd8620)
![image](https://github.com/user-attachments/assets/5bdebd84-645f-4d84-aa72-0766938bfcf7)
 기능을 확인 했을 때 정상적으로 작동하는 것을 확인 할 수 있었습니다.

## 문제점
 이 기능을 활용하기 위해 사용 했던 POP/IMAP 시스템은 보안이 취약하다고 지적을 받았습니다.
 
![image](https://github.com/user-attachments/assets/6b6d88b1-cdec-4bcb-b653-5ad3b92c8dd5)
![image](https://github.com/user-attachments/assets/8c05b8bb-03a1-4891-8e15-c542b29437af)

따라서 이 방법은 공식 프로젝트에서는 사용하기 힘듭니다.

## 느낀점
 같은 기능이라도 다양한 방법이 존재하여 활용할 수 있다고 느꼇습니다. 하지만 일반적으로 사용 하는 방법에는
그 이유가 다 있었습니다. 앞으로 코딩을 할때 이런식으로 다양한 방법을 찾아내고 문제점을 파악하는 것이 중요하다고
느꼇습니다.










