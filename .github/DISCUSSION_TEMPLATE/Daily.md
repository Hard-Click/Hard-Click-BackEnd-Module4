title: "[Daily] YYYY-MM-DD"
labels: ["daily"]
body:
  - type: markdown
    attributes:
      value: |
        ## 📅 데일리 스크럼
 
  - type: textarea
    id: goal
    attributes:
      label: 오늘 목표
      placeholder: 오늘 전체적으로 달성할 목표를 작성해주세요
    validations:
      required: true
 
  - type: markdown
    attributes:
      value: "### 오늘 할 이슈"
 
  - type: input
    id: issue-donghyeon
    attributes:
      label: 강현
      placeholder: "#이슈번호"
 
  - type: input
    id: issue-donghun
    attributes:
      label: 태연
      placeholder: "#이슈번호"
      
  - type: input
    id: issue-jungmin
    attributes:
      label: 종준
      placeholder: "#이슈번호"

  - type: input
    id: issue-jongho
    attributes:
      label: 종호
      placeholder: "#이슈번호"

 
  - type: textarea
    id: notes
    attributes:
      label: 특이사항 / 공유사항
      placeholder: 공유할 내용이 있으면 작성해주세요
