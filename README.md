# Korean-Ultron the PsLM
자바 기반 소규모 언어 모델 **P**owerful small **L**anguage **M**odel

## 학습 데이타
- AI hub 에서 제공하는 말뭉치 데이타
  - 22.인터뷰 진행 멀티턴 데이터
    1. VS_소비자_이해_라이프_스타일
  
## 과제
1. 기본
   - 올바른 활용 데이타 선별
   - 선별된 데이타 더 가치있게 조합
   - 활용 데이타 양산 자동화
   - (+.) 더 빠른 성능
2. 응용 (todo..)
   - 대화 누적 연결 (크기가 큰 입력 데이타 처리 성능 강화)
   - 시간 정보나 사용자별 정보 등의 추가적 변수 반영
   - 전문 분야 정보 저장/효율적 격리

## 참고
```sql
select ifnull(concat(o.word, group_concat(concat(if(c.cnt < c.space, ' ', ''), rw.word) order by uc.i separator '')), o.word) sentence, s.n, s.target
from llm_word o, plm_ultron_sentence s
left join plm_ultron_context uc on s.n = uc.sentence
left join plm_context c on c.n = uc.context
left join llm_word rw on c.rightword = rw.n
where s.opener = o.n
group by s.n
;
select up.word n, up.n i, lead(up.word) over(order by up.n) r from ultron_parameter up
where up.n < 3
```
