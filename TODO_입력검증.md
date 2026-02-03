# 입력 검증 (Validation) - 나중에 처리

> API 구현 완료 후 진행 예정

---

## 1. LoginRequestDto - 검증 추가
- **파일:** `src/main/java/my/domain/user/dto/LoginRequestDto.java`
- **수정:**
```java
@Email(message = "올바른 이메일 형식이 아닙니다")
@NotBlank(message = "이메일은 필수입니다")
private String email;

@NotBlank(message = "비밀번호는 필수입니다")
private String password;
```

---

## 2. BookOwnerJoinRequestDto - 검증 추가
- **파일:** `src/main/java/my/domain/bookowner/dto/BookOwnerJoinRequestDto.java`
- **수정:**
```java
@NotBlank private String name;
@Email @NotBlank private String email;
@Pattern(regexp = "^010-\\d{4}-\\d{4}$") private String phone;
@NotBlank private String password;
@NotBlank private String bankName;
@NotBlank private String accountNumber;
```

---

## 3. Controller에 @Valid 추가
- **파일:** `src/main/java/my/api/auth/AuthApiController.java`
- **수정:**
```java
public ApiResponse<?> login(@RequestBody @Valid LoginRequestDto dto)
public ApiResponse<?> registerBookOwner(@RequestBody @Valid BookOwnerJoinRequestDto dto)
```

---

*작성일: 2026-02-03*
