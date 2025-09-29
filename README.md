# Korean-Ultron the PsLM
자바 기반 소규모 언어 모델 **P**owerful small **L**anguage **M**odel

## 학습 데이타
- AI hub 에서 제공하는 말뭉치 데이타
  - 22.인터뷰 진행 멀티턴 데이터
    1. VS_소비자_이해_라이프_스타일
  
## 캐이스 일지
### 단독 토큰 (`오프너`)
- `입력`: `출력` 데이타가 1:1, 관련 문장 없음 => `자동차` (2079)
- `입력`: `출력` 데이타가 1:1, 관련 문장 존재 => `주제` (743)
- `입력`: `출력` 데이타가 1:0, 관련 문장 존재 => `문장` (398)
- `입력`: `출력` 데이타가 1:N => `게임` (75)
  1. 제미나이 답
  2. 사전

### 복수 토큰 (문장)
todo..

## 참고
```sql
select o.word opener, group_concat(rw.word order by uc.i), s.n, s.target from llm_word o, plm_ultron_sentence s
left join plm_ultron_context uc on s.n = uc.sentence
left join plm_context c on c.n = uc.context
left join llm_word rw on c.rightword = rw.n
where s.opener = o.n
group by s.n
```
