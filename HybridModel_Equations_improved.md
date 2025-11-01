# Model Equations with Detailed Explanations - MVP

## Overview
This document contains the mathematical equations for the **two agents implemented in Phase 3**, with **detailed explanations** of what each equation represents and why it matters.

---

## 1. CAREGIVER AGENT EQUATIONS

### Equation 1.1: Workload Stress Component

$$S_W = \begin{cases}
0.5 & \text{if } H_p < 25 \\
1.5 & \text{if } 25 \leq H_p < 30 \\
3.0 & \text{if } H_p \geq 30
\end{cases}$$

**What It Measures:** How stressed a caregiver becomes based on the number of hours per WEEK they spend providing direct care.

**Why It Matters:** Research shows that caring for a dementia patient becomes increasingly stressful at different workload levels. Caregiving is physically and emotionally taxing—more hours = more stress.

**Explanation:**
- **Less than 25 hours/weeks (Stress = 0.5):** Manageable workload. The caregiver can handle this without significant stress.
- **25-30 hours/week (Stress = 1.5):** Moderate workload. The caregiver is getting fatigued but can manage with some breaks.
- **30+ hours/week (Stress = 3.0):** Overwhelming workload. Nearly full-time caregiving exhausts the caregiver.

**Real-world context:** A 12-hour caregiving day (medication, meals, hygiene, supervision) is unsustainable for most family members.

**Implemented in AnyLogic:**
- **Function:** `calculateStressLevels()`
- **Variable:** `workloadStress`
- **Where:** Caregiver agent

---

### Equation 1.2: Financial Stress Component

$$S_F = \begin{cases}
3.0 & \text{if } I_w < $800 \\
1.5 & \text{if } $800 \leq I_w < $1,300 \\
0.5 & \text{if } $1,300 \leq I_w < 1,900 \\ 
0.0 & \text{if } I_w > 1,900
\end{cases}$$

**What It Measures:** How stressed a caregiver becomes based on weekly family income (financial security).

**Why It Matters:** Financial strain is a major stressor for caregivers. Many cut work hours to provide care, reducing income. Others face medical bills, home modifications, or lost opportunities for earning.

**Explanation:**
- **Less than $800/week (Stress = 3.0):** Financial crisis. The family is struggling to meet basic needs. Very high stress.
- **$800-$1.3k/week (Stress = 1.5):** Moderate financial strain. They can pay bills but with difficulty. Some stress but manageable.
- **$1.3k- 1.9k/week (Stress = 0.5):** Financially stable. The family has cushion for unexpected expenses. Low financial stress.

**Real-world context:** Many caregivers leave their jobs (reducing income) to care for parents/spouses. $1,275/week is roughly the average wage for one person, making caregiving families financially vulnerable.

**Implemented in AnyLogic:**
- **Function:** `calculateStressLevels()`
- **Variable:** `financialStress`
- **Where:** Caregiver agent

---

### Equation 1.3: Sleep Quality Stress Component

$$S_S = \begin{cases}
3.0 & \text{if } S_h < 28 \\ 
1.5 & \text{if } 28 \leq S_h < 42 \\
0.5 & \text{if } 42 \leq S_h < 49 \\
0.0 & \text{if } S_h \geq 49
\end{cases}$$

**What It Measures:** How stressed a caregiver becomes based on hours of sleep per night.

**Why It Matters:** Sleep deprivation is a critical stressor. Dementia patients often have disrupted sleep, waking at night, which forces caregivers to be vigilant (checking on patient, responding to needs). Poor sleep affects immune function, mood, and ability to cope.

**Explanation:**
- **Less than 28 hours/night (Stress = 3.0):** Severe sleep deprivation. The caregiver is exhausted, which impairs judgment and increases vulnerability to illness. Critical stress level.
- **28-42 hours/night (Stress = 1.5):** Moderate sleep deficit. Below recommended 28-49 hours a week, causing fatigue and mood issues.
- **42-49 hours/Week (Stress = 0.5):** Acceptable sleep. Allows recovery and resilience. Low stress.
- *49+ hours/Week (Stress = 0.0):** We ignore anything more than 49+/week for our stress
 
**Real-world context:** Many dementia caregivers average 4-5 hours per night due to patients waking, wandering, or having sundowning episodes.

**Implemented in AnyLogic:**
- **Function:** `calculateStressLevels()`
- **Variable:** `sleepStress`
- **Where:** Caregiver agent

---

### Equation 1.4: Composite Caregiver Stress Level

$$\sigma_C = \frac{S_W + S_F + S_S}{3}$$

**What It Measures:** Overall stress by combining all three stressors into one composite score.

**Why It Matters:** Caregiving stress is multifactorial—it's not just one thing. A caregiver might have high workload stress AND financial stress AND poor sleep. This equation averages all three to get a realistic picture.

**Explanation:**
- We treat workload, finances, and sleep as **equally important** (each 33%)
- The result is a **0-3 scale** (same as individual components)
- Higher numbers indicate more severe stress

**Example calculation:**
- Workload stress: 1.5 (caring 7 hours/day)
- Financial stress: 1.5 (income $12k/month)
- Sleep stress: 1.5 (getting 4.5 hours/night)
- **Total stress: (1.5 + 1.5 + 1.5) / 3 = 1.5** (Moderate stress)

**Stress thresholds that trigger state changes:**
- **0-1.0 = Low stress** → Caregiver coping well, good care quality
- **1.0-2.0 = Moderate stress** → Caregiver struggling a bit, care quality declining
- **2.0-2.7 = High stress** → Caregiver really struggling, care quality poor, risk of burnout
- **2.7+ = Crisis** → Caregiver in emergency situation, quality of life at risk

**Implemented in AnyLogic:**
- **Function:** `calculateStressLevels()`
- **Variable:** `stressLevel`
- **Statechart:** Drives transitions between 6 states (caregivingBehaviour → Low → Moderate → High → Crisis → SeekingSupport)

---

### Equation 1.5: Care Quality Delivered by Caregiver

$$Q_C = \max(0.2, \min(1.0, \; 0.8 - \frac{\sigma_C}{3} \times 0.5 + C_S \times 0.2))$$

**What It Measures:** Quality of care the caregiver provides to the patient, which depends on their stress level and coping skills.

**Why It Matters:** The amount and quality of care a patient receives directly affects their health outcomes. A stressed caregiver provides lower-quality care (rushing, mistakes, neglect). A caregiver with good coping skills provides better care even under stress.

**How It Works (Step by Step):**

1. **Start with base quality: 0.8** 
   - Assumes average caregiver starts with decent capability (80%)

2. **Subtract stress impact: \(\frac{\sigma_C}{3} \times 0.5\)**
   - Stress reduces quality
   - Divided by 3 to normalize stress (0-3 scale) to 0-1
   - Multiplied by 0.5 means stress can reduce quality by maximum 50%
   - Example: If stress = 3.0 (maximum), impact = (3/3) × 0.5 = 0.5 (reduce quality by 50%)

3. **Add coping bonus: \(C_S \times 0.2\)**
   - Coping skills improve quality
   - Scales 0-1, so max improvement is 20%
   - Example: If coping = 1.0 (excellent), bonus = 0.2 (improve by 20%)

4. **Constrain result between 0.2 and 1.0**
   - Minimum 0.2: Even a burned-out caregiver provides SOME care
   - Maximum 1.0: Perfect care quality

**Example Calculation:**
- Stress = 1.5 (Moderate)
- Coping skills = 0.6 (Pretty good)
- Stress impact = (1.5 / 3) × 0.5 = 0.25
- Coping bonus = 0.6 × 0.2 = 0.12
- Quality = 0.8 - 0.25 + 0.12 = **0.67** (Decent care, but impacted by stress)

**Real-world impact:**
- **0.2-0.4 (Poor care):** Patient at risk. Medications missed, hygiene neglected, safety compromised.
- **0.4-0.7 (Moderate care):** Adequate but not optimal. Some issues, some gaps.
- **0.7-1.0 (Good care):** Patient stable and safe. Needs being met, quality of life maintained.

**Implemented in AnyLogic:**
- **Function:** `calculateCareQuality()`
- **Variable:** `careQuality`
- **Where:** Used in patient's disease progression calculation (better care = slower decline)

---

## 2. PATIENT WITH DEMENTIA EQUATIONS

### Equation 2.1: Disease Progression (Weekly Decline)

$$H(t+1) = \max\left(0, \; H(t) - \frac{r_d \times (1 - Q_E \times 0.5)}{100}\right)$$

**What It Measures:** How much worse the patient's health becomes each week due to disease progression.

**Why It Matters:** Dementia is progressive—it always gets worse. But the RATE of decline depends on care quality. Good care can slow it down; poor care speeds it up.

**How It Works:**

1. **Base decline rate: \(r_d = 0.015\)** 
   - Without any care, health declines by 1.5% per week
   - Baseline from dementia disease progression research

2. **Care quality modifier: \((1 - Q_E \times 0.5)\)**
   - \(Q_E\) is effective care quality (0-1)
   - Multiplied by 0.5 means care can reduce decline by MAXIMUM 50%
   - If care quality = 1.0 (perfect), modifier = (1 - 0.5) = 0.5 (only 50% decline occurs)
   - If care quality = 0 (no care), modifier = 1.0 (full decline occurs)

3. **Divided by 100:** Converts percentage to decimal

4. **Constraint: \(\max(0, ...)\)** Ensures health never goes negative

**Example Calculation:**
- Health today = 0.60 (Moderate dementia)
- Effective care quality = 0.8 (Good care)
- Decline = 0.015 × (1 - 0.8 × 0.5) / 100 = 0.015 × 0.6 / 100 = 0.00009
- Health next week = 0.60 - 0.00009 = 0.59991 (slight decline, slowed by good care)

**Real-world impact:**
- With NO care: Health ≈ 0.60 → 0.585 (1.5% decline)
- With GOOD care: Health ≈ 0.60 → 0.599 (0.75% decline, half as fast)
- **Better care = slower progression = patient stays functional longer**

**Implemented in AnyLogic:**
- **Function:** `progressDisease()`
- **Event:** `DiseaseProgression` (runs weekly)
- **Variable:** `healthStatus`
- **Where:** PatientWithDementia agent

---

### Equation 2.2: Dementia Stage Assignment

$$\text{Stage}(H) = \begin{cases}
2 & \text{if } H \geq 0.35 \text{ (Moderate)} \\
3 & \text{if } 0.15 \leq H < 0.35 \text{ (Severe)} \\
4 & \text{if } H < 0.15 \text{ (End-stage)}
\end{cases}$$

**What It Measures:** Which disease stage the patient is in, based on remaining cognitive/functional ability.

**Why It Matters:** Different stages require different types of care. Moderate dementia patients can do some self-care. Severe patients need constant supervision. End-stage requires 24/7 care.

**Explanation:**

- **Health ≥ 0.35 (Moderate Stage):** Patient still has some independence. Can recognize family, communicate basics, do some self-care with reminders. Care needs: ~10 hours/day
- **Health 0.15-0.35 (Severe Stage):** Patient is very dependent. May not recognize family, needs help with all ADLs, high behavioral issues. Care needs: ~15-18 hours/day
- **Health < 0.15 (End-stage):** Patient needs full-time care. Cannot communicate, cannot self-care, bedridden often. Care needs: 20+ hours/day

**Real-world timing:** Average progression is 8-10 years from diagnosis to end-stage, but varies greatly based on care quality and initial health.

**Implemented in AnyLogic:**
- **Function:** `progressDisease()`
- **Statechart:** ModerateDemo → SevereDementia → EndStage
- **Variable:** `dementiaStage`
- **Where:** PatientWithDementia agent

---

### Equation 2.3: Behavioral Symptoms

$$B(H) = \lfloor 10 \times (1 - H) \rfloor$$

**What It Measures:** Severity of behavioral/psychological symptoms (confusion, agitation, aggression, wandering) on a 0-10 scale.

**Why It Matters:** Behavioral symptoms are a major source of caregiver stress. Confused or agitated patients are harder to care for, require more supervision, and are at higher risk of harm.

**Explanation:**
- As health DECLINES (H gets smaller), symptoms WORSEN
- Linear relationship: each 10% decline in health = 1 point worse on symptom scale
- Examples:
  - H = 1.0 (healthy): 0 symptoms
  - H = 0.7 (mild decline): 3 symptoms
  - H = 0.5 (moderate): 5 symptoms
  - H = 0.2 (severe): 8 symptoms
  - H = 0.0 (end-stage): 10 symptoms (maximum)

**Real-world examples:**
- **3 symptoms:** Mild forgetfulness, occasional confusion
- **5 symptoms:** Significant confusion, mood swings, some aggression
- **8 symptoms:** Severe agitation, sundowning, combative behavior
- **10 symptoms:** Severe behavioral issues, completely unpredictable

**Implemented in AnyLogic:**
- **Function:** `updateCareNeeds()`
- **Variable:** `behaviouralSymptoms`
- **Where:** PatientWithDementia agent

---

### Equation 2.4: Care Needs (Hours Per Day)

$$N_C = \max\left(4, \; (4 + (1-H) \times 16) - P_h \times 0.02\right)$$

**What It Measures:** How many hours per day of care the patient needs from their family caregiver.

**Why It Matters:** This is the PRIMARY DRIVER of caregiver workload and stress. As patients decline, they need more care, which cascades into higher caregiver stress.

**How It Works:**

1. **Base formula: \(4 + (1-H) \times 16\)**
   - Starts at 4 hours/day (minimum supervision)
   - As health declines (H decreases), care needs INCREASE
   - Maximum range is 4 + 16 = 20 hours/day
   
2. **Professional care reduction: \(P_h \times 0.02\)**
   - Professional services reduce family burden by 2% per hour
   - Example: 20 hours/week professional care = roughly 3 hours/day average = 3 × 0.02 = 6% reduction
   
3. **Minimum 4 hours: \(\max(4, ...)\)**
   - Even very healthy patients need supervision/check-ins = 4 hours minimum

**Example Calculations:**

*Moderate patient (H = 0.5), no professional help:*
- Base = 4 + (1 - 0.5) × 16 = 4 + 8 = 12 hours/day
- No professional help = 12 hours/day family required

*Severe patient (H = 0.25), WITH 20 hrs/week professional care:*
- Base = 4 + (1 - 0.25) × 16 = 4 + 12 = 16 hours/day
- Professional reduction = 20 × 0.02 = 0.4 hours/day
- Final = 16 - 0.4 = 15.6 hours/day (professional care helps but doesn't eliminate family burden)

**Real-world impact:**
- **4-6 hours/day:** Manageable. Patient mostly independent or in memory care facility
- **8-12 hours/day:** Significant burden. Usually requires someone quitting work
- **15+ hours/day:** Nearly full-time. Caregiver has little personal time

**Implemented in AnyLogic:**
- **Function:** `updateCareNeeds()`
- **Variable:** `careNeedsHours`
- **Updates:** Caregiver's `workloadHoursPerDay` (feedback loop!)
- **Where:** PatientWithDementia agent

---

### Equation 2.5: Effective Care Quality (Blended)

$$Q_E = \max\left(0.1, \; \min\left(1.0, \; \frac{Q_C \times H_f + 0.9 \times P_h}{H_f + P_h}\right)\right)$$

**What It Measures:** Overall quality of care received, blending family caregiver quality with professional care quality.

**Why It Matters:** Dementia patients often receive care from BOTH family AND professionals (day programs, home care). This equation shows how they work together.

**How It Works:**

1. **Family contribution: \(Q_C \times H_f\)**
   - \(Q_C\) = caregiver's care quality (variable, affected by stress)
   - \(H_f\) = family care hours/week (≈ caregiver quality × 40 hours max)
   - Example: If caregiver quality = 0.7, available family hours ≈ 0.7 × 40 = 28 hours/week

2. **Professional contribution: \(0.9 \times P_h\)**
   - Professionals assumed to be high quality (0.9 = 90%)
   - \(P_h\) = professional hours/week
   - Example: 20 hours/week adult day program = 0.9 × 20 = 18 quality-hours

3. **Weighted average: \(\frac{\text{family} + \text{professional}}{\text{total hours}}\)**
   - Combines both to get overall effectiveness
   - Example: (0.7 × 28 + 0.9 × 20) / (28 + 20) = (19.6 + 18) / 48 = 0.78 (Good care!)

4. **Constrain 0.1-1.0:**
   - Minimum 0.1: Even with poor family care and no professional, some care happens
   - Maximum 1.0: Perfect care is not possible (always room for improvement)

** potential scenarios:**

**Scenario A: Family-only care (no professional services)**
- Family care quality = 0.6 (stressed caregiver)
- Professional = 0 hours
- Effective quality = 0.6 (LIMITED - depends entirely on family)

**Scenario B: Family + Adult Day Program**
- Family care quality = 0.6
- Family hours = 0.6 × 40 = 24/week
- Professional hours = 20/week (day program)
- Effective = (0.6 × 24 + 0.9 × 20) / 44 = (14.4 + 18) / 44 = **0.74** (Better!)



**Implemented in AnyLogic:**
- **Function:** `calculateEffectiveCareQuality()`
- **Variable:** `effectiveCareQuality`
- **Used in:** Disease progression calculation (Equation 2.1)
- **Where:** PatientWithDementia agent

---

## 3. THE FEEDBACK LOOP: How Everything Connects

### The Vicious Cycle (Without Intervention)

$$\sigma_C \rightarrow Q_C \rightarrow Q_E \rightarrow H(t) \rightarrow N_C \rightarrow H_p \rightarrow \sigma_C$$

**What It Shows:** How caregiver stress and patient health create a vicious cycle.

**The Flow:**

1. **Caregiver stress (\(\sigma_C\)) is HIGH**
2. **↓ Care quality (\(Q_C\)) DROPS** (stressed people provide worse care)
3. **↓ Effective quality (\(Q_E\)) DROPS** (no professional help to compensate)
4. **↓ Patient health (\(H(t)\)) DECLINES FASTER** (poor care accelerates decline)
5. **↓ Care needs (\(N_C\)) INCREASE** (sicker patient needs more hours)
6. **↓ Caregiver workload (\(H_p\)) INCREASES** (more hours to provide)
7. **↓ Caregiver stress (\(\sigma_C\)) GOES UP** (via Equation 1.1, more hours = more workload stress)
8. **LOOP REPEATS → Spiral downward!**

**Without intervention, this cycle gets worse and worse until caregiver burnout or patient crisis.**

### Breaking the Cycle (With Intervention)

**Professional services break the cycle:**
- Reduce \(H_p\) (family hours decrease)
- Increase \(Q_E\) (professional quality adds 0.9)
- Result: \(\sigma_C\) DROPS (less workload stress)
- \(Q_C\) IMPROVES (less stressed caregiver, better care)
- Patient decline SLOWS
- Cycle stabilizes

**This is why professional services WORK—they break the negative feedback loop.**

---

## 4. VARIABLE REFERENCE TABLE

| Symbol | AnyLogic Variable | Type | Range | What It Means |
|--------|---|---|---|---|
| \(\sigma_C\) | `stressLevel` | double | 0–3 | Overall caregiver stress |
| \(S_W\) | `workloadStress` | double | 0–3 | Stress from care hours |
| \(S_F\) | `financialStress` | double | 0–3 | Stress from income |
| \(S_S\) | `sleepStress` | double | 0–3 | Stress from poor sleep |
| \(Q_C\) | `careQuality` | double | 0.2–1.0 | Quality of family care |
| \(C_S\) | `copingSkills` | double | 0–1 | Caregiver's coping ability |
| \(H_p\) | `workloadHoursPerDay` | double | 0–24 | Hours family spends on care |
| \(I_m\) | `familyIncomeMonthly` | double | 0+ | Monthly family income |
| \(S_h\) | `sleepQualityHours` | double | 0–12 | Hours of sleep/night |
| \(H(t)\) | `healthStatus` | double | 0–1 | Patient's health (1=healthy, 0=end-stage) |
| \(B\) | `behaviouralSymptoms` | int | 0–10 | Behavioral symptom severity |
| \(N_C\) | `careNeedsHours` | double | 4–20 | Hours of care needed/day |
| \(Q_E\) | `effectiveCareQuality` | double | 0.1–1.0 | Combined family+professional care quality |
| \(P_h\) | `professionalCareHours` | double | 0+ | Hours of professional care/week |
| \(r_d\) | `progressionRate` | double | 0.015 | Disease decline rate/week |

---

## 5. SUMMARY: How the Model Works

1. **Caregiver stress** is calculated from workload, finances, and sleep (Eq 1.1-1.4)
2. **Caregiver stress** affects **care quality** (Eq 1.5)
3. **Care quality** (combined with professional care) creates **effective care quality** (Eq 2.5)
4. **Effective care quality** affects **patient health decline** (Eq 2.1)
5. **Patient health** determines **stage** (Eq 2.2) and **behavioral symptoms** (Eq 2.3)
6. **Patient health** determines **care needs** (Eq 2.4)
7. **Care needs** update **caregiver workload** (feedback loop)
8. **Loop repeats weekly** (vicious cycle spirals or stabilizes)


---
** CIHI (2025) - Unpaid Caregiver Challenges and Supports**
Source: Canadian Institute for Health Information (CIHI)

URL: https://www.cihi.ca/en/dementia-in-canada/unpaid-caregiver-challenges-and-supports

Date: 2025 (Latest)

What it supports:

- 26 hours/week Canadian dementia caregivers (vs 17 for other seniors)

- 45% experience distress (vs 26% for other caregivers)

- $1.4 billion total out-of-pocket costs for Canadian caregivers

- Behavioural/verbal aggression increases distress by 1.6×

** Chambers et al. (2005) - Research on Alzheimer's Caregiving in Canada**
Source: Health Promotion and Chronic Disease Prevention in Canada (Peer-reviewed)

URL: https://www.canada.ca/en/public-health/services/reports-publications/health-promotion-chronic-disease-prevention-canada-research-policy-practice/vol-25-no-3-2004/research-on-alzheimercaregiving-canada.html

What it supports:

- 63 hours/month informal assistance from primary caregivers (Canadian Study of Health and Aging - CSHA)

- Family members provide 75-85% of care for frail Canadian seniors

- Caregiving is evolving long-term process with predictable transitions

- Evidence-based interventions studied in Canadian context

** Alzheimer Society Canada - Research Studies & Data**
Source: Alzheimer Society Canada (National organization)

URL: https://alzheimer.ca/find-studies/studies

What it supports:

- 61% of Canadians with dementia live at home

- 87% have informal caregiver support at home

- Canadian-specific dementia progression patterns

- Young onset dementia caregivers face unique challenges

** Statistics Canada / CCCE - Caregiving in Canada (2022)**
Source: Canadian Centre for Caregiving Excellence (CCCE) Report

URL: https://canadiancaregiving.org/canadian-caregivers-are-at-a-breaking-point/

What it supports:

- 1 in 4 Canadians is a caregiver; 1 in 2 will become one

- 7.8 million Canadians provide ~20 hours/week unpaid care

- 5.7 billion hours/year of unpaid care ($97.1B value)

- Caregivers at breaking point - burnout, financial strain

**Last Updated:** Novemebr 1, 2025
