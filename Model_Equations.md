# Dementia & Caregiver Model - Mathematical Equations

## Overview
This document contains all mathematical equations used in the hybrid Agent-Based Model (ABM) + Discrete Event Simulation (DES) of dementia caregiving stress and patient outcomes.

---

## 1. CAREGIVER AGENT EQUATIONS

### 1.1 Workload Stress Component

$$S_W = \begin{cases}
0.5 & \text{if } H_p < 5 \text{ hours/day (Low)} \\
1.5 & \text{if } 5 \leq H_p < 10 \text{ hours/day (Moderate)} \\
3.0 & \text{if } H_p \geq 10 \text{ hours/day (High)}
\end{cases}$$

**Where:**
- $S_W$ = Workload stress component (0–3 scale)
- $H_p$ = Hours per day spent on patient care

**Explanation:** Caregiver stress from workload is categorized into three levels. Less than 5 hours is manageable (low stress). 5–10 hours is moderate but sustainable. Over 10 hours is unsustainable (high stress).

---

### 1.2 Financial Stress Component

$$S_F = \begin{cases}
3.0 & \text{if } I_m < 10,000 \text{ (High strain)} \\
1.5 & \text{if } 10,000 \leq I_m < 15,000 \text{ (Moderate)} \\
0.5 & \text{if } I_m \geq 15,000 \text{ (Low)}
\end{cases}$$

**Where:**
- $S_F$ = Financial stress component (0–3 scale)
- $I_m$ = Monthly family income (USD)

**Explanation:** Financial stress reflects income adequacy. Families earning less than \$10k/month face high stress. \$10–15k is moderate. Over \$15k provides financial cushion (low stress).

---

### 1.3 Sleep Quality Stress Component

$$S_S = \begin{cases}
3.0 & \text{if } S_h < 4 \text{ hours/night (Poor)} \\
1.5 & \text{if } 4 \leq S_h < 5 \text{ hours/night (Moderate)} \\
0.5 & \text{if } S_h \geq 5 \text{ hours/night (Good)}
\end{cases}$$

**Where:**
- $S_S$ = Sleep quality stress component (0–3 scale)
- $S_h$ = Hours of sleep per night

**Explanation:** Less than 4 hours causes severe stress (exhaustion). 4–5 hours is moderate but impacts health. 5+ hours allows recovery and is sustainable.

---

### 1.4 Composite Caregiver Stress Level

$$\sigma_C = \frac{S_W + S_F + S_S}{3}$$

**Where:**
- $\sigma_C$ = Composite stress level (0–3 scale)
- All components equally weighted (33.33% each)

**Range:** 0–3

**Distress State Classification:**
$$\text{Distress State} = \begin{cases}
\text{Low} & \text{if } \sigma_C < 1.0 \\
\text{Moderate} & \text{if } 1.0 \leq \sigma_C < 2.0 \\
\text{High} & \text{if } 2.0 \leq \sigma_C < 2.7 \\
\text{Crisis} & \text{if } \sigma_C \geq 2.7
\end{cases}$$

**Explanation:** Average the three stress components equally. Each component contributes equally to overall caregiver burden.

---

### 1.5 Care Quality Delivered by Caregiver

$$Q_C = \text{clamp}_{[0.2, 1.0]} \left( 0.8 - \frac{\sigma_C}{3} \times 0.5 + C_S \times 0.2 \right)$$

**Where:**
- $Q_C$ = Quality of care delivered (0–1 scale)
- $\sigma_C$ = Caregiver stress level
- $C_S$ = Coping skills (0–1 scale)
- Base quality = 0.8
- Stress impact = max 50% reduction
- Coping boost = max 20% improvement

**Explanation:**
- Start with baseline quality of 0.8
- High stress reduces quality (up to 50%)
- Coping skills improve quality (up to 20%)
- Clamp result between minimum 0.2 and maximum 1.0

---

## 2. PATIENT WITH DEMENTIA EQUATIONS

### 2.1 Disease Progression (Weekly Decline)

$$H(t+1) = \max \left( 0, H(t) - \frac{r_d \times (1 - Q_E \times 0.5)}{100} \right)$$

**Where:**
- $H(t)$ = Patient health status at time $t$ (0–1 scale)
- $H(t+1)$ = Patient health status at time $t+1$
- $r_d$ = Baseline disease progression rate (default: 0.015 per week)
- $Q_E$ = Effective care quality (0–1 scale)
- Factor 0.5 = care quality can slow decline by up to 50%

**Time unit:** Weeks

**Explanation:**
- Patient health naturally declines each week (disease progression)
- Better care quality ($Q_E$) slows decline
- At $Q_E = 1.0$ (perfect care), decline reduced by 50%
- At $Q_E = 0.0$ (no care), decline is at full rate
- Health clamped at minimum 0 (cannot be negative)

---

### 2.2 Dementia Stage Assignment

$$\text{Stage}(H) = \begin{cases}
2 \text{ (Moderate)} & \text{if } H \geq 0.35 \\
3 \text{ (Severe)} & \text{if } 0.15 \leq H < 0.35 \\
4 \text{ (End-stage)} & \text{if } H < 0.15
\end{cases}$$

**Where:**
- $H$ = Health status (0–1)

**Explanation:**
- Model starts patients at Moderate stage (MVP simplification)
- Health ≥ 0.35 = Moderate cognitive/functional loss
- Health 0.15–0.35 = Significant support needed (Severe)
- Health < 0.15 = End-of-life care (End-stage)

---

### 2.3 Behavioral Symptoms

$$B(H) = \lfloor 10 \times (1 - H) \rfloor$$

**Where:**
- $B$ = Behavioral symptoms severity (0–10 integer scale)
- $H$ = Health status (0–1)
- $\lfloor \cdot \rfloor$ = Floor function (round down)

**Explanation:**
- Symptoms increase as health declines
- At $H = 1.0$ (healthy): 0 symptoms
- At $H = 0.5$ (moderate): 5 symptoms
- At $H = 0.0$ (end-stage): 10 symptoms (maximum)

---

### 2.4 Care Needs (Hours Per Day)

$$N_C = \max \left( 4, (4 + (1 - H) \times 16) - P_h \times 0.02 \right)$$

**Where:**
- $N_C$ = Care needs (hours/day)
- $H$ = Patient health status
- $P_h$ = Professional care hours/week
- Base minimum = 4 hours/day
- Maximum range without professional = 4 + 16 = 20 hours/day
- Professional care reduction = 2% per hour

**Explanation:**
- Healthy patient ($H = 1.0$): needs 4 hours/day (basic supervision)
- End-stage patient ($H = 0.0$): needs up to 20 hours/day (24/7 care)
- Professional services reduce family burden (2% per professional hour)
- Always maintain minimum 4 hours/day (safety margin)

---

### 2.5 Effective Care Quality (Blended Family + Professional)

$$Q_E = \text{clamp}_{[0.1, 1.0]} \left( \frac{Q_C \times H_f + 0.9 \times P_h}{H_f + P_h} \right)$$

**Where:**
- $Q_E$ = Effective care quality (0–1 scale)
- $Q_C$ = Caregiver's care quality (0–1)
- $H_f$ = Family care hours/week ($\approx Q_C \times 40$)
- $P_h$ = Professional care hours/week
- Professional care quality assumed = 0.9 (high)

**Explanation:**
- Family contribution: $Q_C \times H_f$ (caregiver quality × available hours)
- Professional contribution: $0.9 \times P_h$ (professional quality × hours)
- Weighted average of both contributions
- Professional care is assumed higher quality (0.9) than family (variable)
- Clamped between 0.1 (minimum) and 1.0 (maximum)

---

## 3. FEEDBACK LOOPS & COUPLING EQUATIONS

### 3.1 Caregiver Workload Updates from Patient

$$H_p(t+1) \leftarrow N_C(t)$$

**Where:**
- $H_p$ = Caregiver's hours/day (updates weekly)
- $N_C$ = Patient's care needs (output from Eq. 2.4)

**Explanation:** Patient's increasing care needs directly increase caregiver workload, which feeds back into stress calculation.

---

### 3.2 Bidirectional Feedback Loop

$$\sigma_C \rightarrow Q_C \rightarrow Q_E \rightarrow H(t+1) \rightarrow N_C \rightarrow H_p \rightarrow \sigma_C$$

**Explanation of the feedback cascade:**
1. Caregiver stress $\sigma_C$ reduces care quality $Q_C$
2. Reduced quality lowers effective care $Q_E$
3. Worse care speeds patient decline $H(t+1)$
4. Declining health increases care needs $N_C$
5. Increased needs increase caregiver workload $H_p$
6. Workload stress increases $\sigma_C$ again

**This creates a vicious cycle that interventions (professional care, support groups) must break.**

---

## 4. INTERVENTION EFFECT EQUATIONS

### 4.1 Professional Care Impact

$$P_h = \begin{cases}
20 & \text{if adult day program enrolled} \\
20 + X & \text{if home care of } X \text{ hours/week added} \\
0 & \text{if no professional services}
\end{cases}$$

**Explanation:**
- Adult day program provides 20 hours/week base
- Home care can be added on top (typically 10 hours/week)
- Total professional care $P_h$ used in Equations 2.4 and 2.5

---

### 4.2 Stress Relief from Service

$$\sigma_C^{\text{after}} = \max(0, \sigma_C^{\text{before}} - \Delta\sigma)$$

**Where:**
- $\Delta\sigma$ = Stress reduction from service (default: 0.3)

**Explanation:** When caregiver receives respite care, stress is reduced by fixed amount (typically 30% reduction).

---

## 5. VARIABLE DEFINITIONS TABLE

| Symbol | Meaning | Range | Units |
|--------|---------|-------|-------|
| $\sigma_C$ | Caregiver stress level | 0–3 | scale |
| $S_W$ | Workload stress component | 0–3 | scale |
| $S_F$ | Financial stress component | 0–3 | scale |
| $S_S$ | Sleep stress component | 0–3 | scale |
| $Q_C$ | Caregiver care quality | 0–1 | scale |
| $H$ | Patient health status | 0–1 | scale |
| $B$ | Behavioral symptoms | 0–10 | integer scale |
| $N_C$ | Care needs | 4–20 | hours/day |
| $Q_E$ | Effective care quality | 0.1–1.0 | scale |
| $H_p$ | Caregiver hours on patient | 0–24 | hours/day |
| $I_m$ | Monthly family income | 0+ | USD |
| $S_h$ | Sleep hours per night | 0–12 | hours |
| $P_h$ | Professional care hours | 0+ | hours/week |
| $C_S$ | Coping skills | 0–1 | scale |
| $r_d$ | Disease progression rate | 0.015 | per week |

---

## Notes

- All equations implemented in AnyLogic using Java
- Time unit is weeks (default simulation duration: 5 years = 260 weeks)
- All continuous variables use double precision floats
- All discrete variables use int type where appropriate
- Clamping function ensures variables stay within valid ranges

---

**Last Updated:** October 28, 2025
**Team:** CMPT 394 - Team 5 (Dementia & Caregiver Project)
