---
description: GitHub Pull Request를 종합적으로 코드리뷰합니다
argument-hint: <pr-number>
allowed-tools: Bash(gh:*), Bash(git:*), Read, Grep, Glob
---

# Pull Request Comprehensive Review

Perform comprehensive code review of a GitHub Pull Request.

## Prerequisites Check

First, verify GitHub CLI is installed and authenticated:

!`gh --version`
!`gh auth status`

## PR Context Collection

Collect PR information:

!`gh pr view $1 --json title,body,author,url,state,headRefName,baseRefName,createdAt`
!`gh pr diff $1`
!`gh pr checks $1`
!`gh pr view $1 --comments`

## Review Instructions

You are comprehensively reviewing **PR #$1** for a **Spring Boot 3.5.9 + Java 21 based DDD architecture** project.

**IMPORTANT: All review output must be written in Korean (한글).**

---

## PR Metadata Review

### PR Information Analysis

**Checklist**:
- [ ] **PR Title**: Clearly describes changes?
- [ ] **PR Description**: Follows PR template format (@.github/PULL_REQUEST_TEMPLATE.md)
- [ ] **Related Issue**: Linked issue exists?
- [ ] **Branch Naming**: Follows naming conventions?
- [ ] **Author**: Who submitted?
- [ ] **Status**: OPEN / MERGED / CLOSED

---

## CI/CD Status Check

Check project CI pipeline:
- @.github/workflows/ci.yml

**Automated validation items**:
- [ ] **Build**: Build successful
- [ ] **Tests**: All tests passing
- [ ] **Checkstyle**: Code style validation passed (max 100 warnings)
- [ ] **JaCoCo**: Code coverage report generated (minimum threshold 0%)

**If CI fails**:
- Identify CI failure cause first
- CI must pass before code review

---

## Code Changes Review

Apply all review criteria from `/commit-review` and `/domain-review` to all PR changes.

### 1. Convention Compliance

Reference project conventions:
- @docs/convention/ARCHITECTURE.md
- @docs/convention/API_CONVENTION.md
- @docs/convention/CODE_STYLE.md
- @docs/convention/CODING_GUIDE.md

**Checklist**:
- [ ] Naming conventions followed
- [ ] Checkstyle rules compliant
- [ ] Style consistent with existing codebase

---

### 2. DDD Architecture (Package Structure)

**Checklist**:
- [ ] Changes respect layer boundaries
- [ ] New files in correct packages
- [ ] No architectural violations introduced

---

### 3. API Design (if new endpoints added)

**REST conventions compliance**:
- [ ] HTTP methods used appropriately (GET/POST/PUT/PATCH/DELETE)
- [ ] RESTful URL design
- [ ] `CommonApiResponse<T>` wrapper used
- [ ] Appropriate HTTP status codes
- [ ] Swagger documentation complete (`@Tag`, `@Operation`, `@Schema`)
- [ ] `SuccessCode`, `ErrorCode` enums updated (if needed)

---

### 4. Database Changes (if schema/Entity changed)

**Checklist**:
- [ ] Migration scripts provided (if needed)
- [ ] Entity relationships correctly defined
- [ ] No breaking changes (or clearly documented)
- [ ] Indexing considered
- [ ] Data integrity maintained

---

### 5. Testing

**Unit Tests**:
- [ ] Unit tests for new Services
- [ ] Business logic verified
- [ ] Edge cases covered

**Integration Tests**:
- [ ] Controller tests for new endpoints (if needed)
- [ ] Repository tests (for complex queries)

**Test Quality**:
- [ ] Test naming conventions followed
- [ ] Given-When-Then pattern (recommended)
- [ ] Appropriate test coverage

---

### 6. Security (Vulnerabilities)

**Checklist**:
- [ ] **Secrets**: No hardcoded passwords/tokens
- [ ] **Input Validation**: Validation exists for all inputs
- [ ] **SQL Injection**: Parameter binding used
- [ ] **XSS**: Proper input validation and output encoding
- [ ] **Sensitive Data**: Not exposed in logs
- [ ] **Authentication/Authorization**: Security settings on protected endpoints (if needed)

---

### 7. Performance Considerations

**Checklist**:
- [ ] **N+1 Query Problem**: Careful with lazy loading, consider fetch join
- [ ] **Transaction Scope**: `@Transactional` used appropriately
- [ ] **Lazy Loading**: Configured correctly
- [ ] **No Performance Anti-patterns**:
  - External API calls inside transactions
  - Unnecessary full table scans
  - Inefficient loops

---

### 8. Documentation

**Checklist**:
- [ ] **Swagger/OpenAPI**: API docs updated
- [ ] **README**: Updated if needed
- [ ] **Complex Logic**: Explained with comments
- [ ] **JavaDoc**: JavaDoc for public APIs (recommended)

---

### 9. Backward Compatibility

**Checklist**:
- [ ] No breaking changes to existing APIs
- [ ] Database migrations are safe
- [ ] Feature flags used (for large changes)
- [ ] Deployment strategy considered (if needed)

---

### 10. Code Quality

**Unnecessary Complexity**:
- [ ] No over-abstraction
- [ ] Code is maintainable
- [ ] DRY principle followed (no duplication)
- [ ] Proper error handling
- [ ] Logging used appropriately

---

## CodeRabbit Integration Check

Project uses CodeRabbit automated review:
- Config: @.coderabbit.yaml
- Profile: assertive (detailed review)
- Language: Korean
- Draft PR auto-review: enabled

**Reference CodeRabbit Comments**:
- If CodeRabbit already reviewed, check results
- Compare with your review for missed items
- If opinion differs from CodeRabbit, provide reasoning

---

## Output Format

**CRITICAL: Write all review results in Korean (한글).**

Structure your PR review as follows:

### 📋 PR Summary: #$1

- **제목**: [PR 제목]
- **작성자**: [작성자 이름]
- **브랜치**: [head 브랜치] → [base 브랜치]
- **상태**: [OPEN / MERGED / CLOSED]
- **CI 상태**: [PASSING / FAILING - 세부 내용]
- **변경 파일**: X개
- **추가/삭제 라인**: +X/-Y

---

### 🎯 Overall Assessment

**추천 액션**: [APPROVE ✅ / REQUEST CHANGES ⚠️ / COMMENT 💬]

**심각도**: [CRITICAL / HIGH / MEDIUM / LOW]

**리뷰 시간**: 약 X분 소요 예상

**한 줄 평가**: [전체적인 PR 품질에 대한 요약]

---

### 🚨 Critical Issues (Must Fix)

Issues that must be fixed:

#### 1. [카테고리] - 파일:라인

- **문제**: [구체적 설명]
- **영향**: [왜 이것이 critical인지]
- **수정 방법**: [구체적 수정 방안]

---

### ⚠️ Major Issues (Should Fix)

Recommended fixes:

#### 1. [카테고리] - 파일:라인

- **문제**: [구체적 설명]
- **영향**: [왜 수정이 권장되는지]
- **수정 방법**: [구체적 수정 방안]

---

### 💡 Minor Issues (Nice to Have)

Nice-to-have improvements:

#### 1. [카테고리] - 파일:라인

- **제안**: [개선 제안]
- **이유**: [왜 개선하면 좋은지]

---

### ✅ Positive Aspects

Well-implemented parts:
- [잘 구현된 패턴]
- [좋은 테스트 커버리지]
- [깔끔한 코드 예시]

---

### ❓ Questions for Author

Questions requiring clarification:
1. [설계 결정에 대한 질문]
2. [추가 컨텍스트 요청]

---

### 🎨 Suggested Improvements (개선안)

#### 아키텍처 개선:
1. [구조적 개선 제안]
2. [레이어 분리 개선]

#### 성능 개선:
1. [성능 최적화 제안]
2. [쿼리 개선]

#### 유지보수성 개선:
1. [장기적 코드 건강성 개선]
2. [리팩토링 제안]

#### 테스트 개선:
1. [테스트 커버리지 갭]
2. [추가 테스트 케이스 제안]

---

### ✓ Review Checklist Summary

- [ ] 컨벤션 준수
- [ ] 아키텍처 경계
- [ ] API 설계 (해당 시)
- [ ] 데이터베이스 변경 (해당 시)
- [ ] 테스트 커버리지
- [ ] 보안
- [ ] 성능
- [ ] 문서화
- [ ] 하위 호환성
- [ ] 코드 품질

**전체 점수**: X/10

---

## Usage Examples

```bash
/pr-review 6
/pr-review 15
```

---

## Review Process

1. **Check PR metadata**: Collect PR info with gh pr view
2. **Check CI status**: Verify automated validation results with gh pr checks
3. **Analyze code changes**: Review full diff with gh pr diff
4. **Read files**: Analyze changed files in detail with Read tool
5. **Reference conventions**: Refer to @docs/convention/ documents
6. **Verify checklist**: Review all categories above
7. **Compare with CodeRabbit**: Compare with automated review results
8. **Provide comprehensive feedback**: Categorize as Critical/Major/Minor issues and provide improvement suggestions (in Korean)

---

## Special Considerations

### Breaking Changes
- Clearly mark API changes
- Need for migration guide
- Need for version update

### Large PRs
- If PR too large, suggest splitting
- Evaluate if size is reviewable

### Urgent Hotfixes
- For hotfixes, review quickly but thoroughly
- Pay special attention to security issues

### First-time Contributors
- For new team member's first PR, provide educational feedback
- Maintain positive and constructive tone
