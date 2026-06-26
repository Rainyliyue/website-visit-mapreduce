# Design Spec — MapReduce Demo 4min

## I. Deck Goal

Create a 4-minute live-demo presentation for the course project “基于 Hadoop MapReduce 的网站访问量统计分析系统”. The deck should help the presenter explain what the system does, how MapReduce is used, how the Web demo should be operated, and which acceptance points have been completed.

## II. Audience

Course teacher / evaluator. The audience cares about whether the project matches the task book, whether Hadoop MapReduce is genuinely included, whether the Web functions can be demonstrated, and whether the implementation is understandable.

## III. Mode

`briefing`: neutral, complete, scannable. Page titles name topics. The notes should sound like a concise project walkthrough, with concrete demo instructions.

## IV. Visual Style

`data-journalism`: restrained dashboard-like information design. Use clean cards, thin dividers, compact tables, numbered steps, and data-system diagrams. Avoid decorative spectacle.

## V. Colors

Background: #F3F6FA  
Surface: #FFFFFF  
Primary: #0F766E  
Primary dark: #0B4F4A  
Secondary: #155E75  
Accent: #F97316  
Success: #16A34A  
Danger: #DC2626  
Text: #152033  
Muted: #667085  
Line: #D9E2EC  
Soft fill: #E8F3F1  
Blue fill: #E8F1F8  

## VI. Typography

Font family: Microsoft YaHei, Arial, sans-serif  
Cover title: 44  
Page title: 34  
Section title: 24  
Body: 20  
Small: 16  
Caption: 14

## VII. Layout

Canvas: ppt169, 1280 x 720.  
Margins: 56 px left/right, 44 px top/bottom.  
Use 2 or 3 column layouts for technical explanation. Use status cards for acceptance points. Keep charts conceptual and manually drawn, not data-driven.

## VIII. Demo Flow

Slide 1: cover and project identity.  
Slide 2: system architecture from log data to charts.  
Slide 3: data fields and analysis dimensions.  
Slide 4: MapReduce core algorithms.  
Slide 5: user-side live demo flow.  
Slide 6: admin-side live demo flow and performance optimization.  
Slide 7: acceptance checklist and closing.

## IX. Slide Plan

1. 项目目标与任务要求  
Core message: what the course task asks for and what this project implements.

2. 系统整体架构  
Core message: the full path from CSV access logs, MapReduce jobs, Spring Boot services, MySQL storage, to ECharts visualization.

3. 数据字段与分析维度  
Core message: every access record contains IP, region, time, and target URL; different analyses read the fields they need.

4. MapReduce 核心算法  
Core message: three jobs share parsing and reducer logic, but use different keys for ranking, peak, and source distribution.

5. 用户功能演示流程  
Core message: how to demonstrate login, task submission, task status, charts, and result details as a normal user.

6. 管理员功能与性能优化  
Core message: how to demonstrate user, task, data, and cache management, plus the performance changes made for larger samples.

7. 验收要点与总结  
Core message: what artifacts and runtime evidence prove that the course requirements are covered.

