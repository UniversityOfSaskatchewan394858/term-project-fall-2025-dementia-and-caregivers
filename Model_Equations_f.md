# Dementia & Caregiver Model - Equations with Code Mapping

## Overview
This document contains the mathematical equations used in the model, with **direct mapping to AnyLogic implementation**.

---

## 1. CAREGIVER AGENT EQUATIONS

### Equation 1.1: Workload Stress Component

$$S_W = \begin{cases}
0.5 & \text{if } H_p < 5 \\
1.5 & \text{if } 5 \leq H_p < 10 \\
3.0 & \text{if } H_p \geq 10
\end{cases}$$

**Explanation:** Stress increases with care hours. Less than 5 hours is manageable (0.5). Between 5-10 hours is moderate (1.5). More than 10 hours is overwhelming (3.0).

**Implemented in AnyLogic:**
- **Function:** `calculateStressLevels()`
- **Variable:** `workloadStress`
- **Where:** Caregiver agent
- **Code logic:** If-else statements checking `workloadHoursPerDay`

---

### Equation 1.2: Financial Stress Component

$$S_F = \begin{cases}
3.0 & \text{if } I_m < 10,000 \\
1.5 & \text{if } 10,000 \leq I_m < 15,000 \\
0.5 & \text{if } I_m \geq 15,000
\end{cases}$$

**Explanation:** Lower income = higher stress. Families earning less than $10k/month are in crisis (3.0). $10-15k is moderate (1.5). Over $15k provides cushion (0.5).

**Implemented in AnyLogic:**
- **Function:** `calculateStressLevels()`
- **Variable:** `financialStress`
- **Where:** Caregiver agent
- **Code logic:** If-else on `familyIncomeMonthly`

---

### Equation 1.3: Sleep Quality Stress Component

$$S_S = \begin{cases}
3.0 & \text{if } S_h < 4 \\
1.5 & \text{if } 4 \leq S_h < 5 \\
0.5 & \text{if } S_h \geq 5
\end{cases}$$

**Explanation:** Poor sleep = high stress. Under 4 hours is critical (3.0). 4-5 hours is moderate (1.5). 5+ hours is healthy (0.5).

**Implemented in AnyLogic:**
- **Function:** `calculateStressLevels()`
- **Variable:** `sleepStress`
- **Where:** Caregiver agent
- **Code logic:** If-else on `sleepQualityHours`

---

### Equation 1.4: Composite Caregiver Stress Level

$$\sigma_C = \frac{S_W + S_F + S_S}{3}$$

**Explanation:** Average of three components. Each weighted equally (33% each). Results in 0-3 scale.

**Implemented in AnyLogic:**
- **Function:** `calculateStressLevels()`
- **Variable:** `stressLevel`
- **Code:** `(workloadStress + financialStress + sleepStress) / 3.0`

**Distress State Classification:**

$$\text{State}(\sigma_C) = \begin{cases}
\text{Low} & \text{if } \sigma_C < 1.0 \\
\text{Moderate} & \text{if } 1.0 \leq \sigma_C < 2.0 \\
\text{High} & \text{if } 2.0 \leq \sigma_C < 2.7 \\
\text{Crisis} & \text{if } \sigma_C \geq 2.7
\end{cases}$$

**Implemented in:** Caregiver statechart (6 states with transitions based on stress thresholds)

---

### Equation 1.5: Care Quality from Caregiver

$$Q_C = \max(0.2, \min(1.0, \; 0.8 - \frac{\sigma_C}{3} \times 0.5 + C_S \times 0.2))$$

**Explanation:**
- Base quality: 0.8
- Stress reduces it: Up to 50% (multiplied by normalizing stress to 0-1)
- Coping improves it: Up to 20%
- Constrained: Between 0.2 (minimum quality) and 1.0 (perfect quality)

**Implemented in AnyLogic:**
- **Function:** `calculateCareQuality()`
- **Variable:** `careQuality`
- **Code:**
```java
double stressImpact = (stressLevel / 3.0) * 0.5;
double copingBoost = copingSkills * 0.2;
double quality = 0.8 - stressImpact + copingBoost;
return Math.max(0.2, Math.min(1.0, quality));
```

---

## 2. PATIENT WITH DEMENTIA EQUATIONS

### Equation 2.1: Disease Progression (Weekly Decline)

$$H(t+1) = \max\left(0, \; H(t) - \frac{r_d \times (1 - Q_E \times 0.5)}{100}\right)$$

**Explanation:**
- $r_d$: Baseline progression rate (0.015 per week)
- $Q_E$: Effective care quality (0-1)
- Care quality can slow decline by up to 50%
- Health never goes below 0 (cannot be negative)

**Implemented in AnyLogic:**
- **Function:** `progressDisease()`
- **Event:** `DiseaseProgression` (weekly, cyclic)
- **Variable:** `healthStatus`
- **Code:**
```java
double decline = progressionRate * (1.0 - effectiveCareQuality * 0.5);
healthStatus = Math.max(0, healthStatus - decline / 100.0);
```

---

### Equation 2.2: Dementia Stage Assignment

$$\text{Stage}(H) = \begin{cases}
2 & \text{if } H \geq 0.35 \text{ (Moderate)} \\
3 & \text{if } 0.15 \leq H < 0.35 \text{ (Severe)} \\
4 & \text{if } H < 0.15 \text{ (End-stage)}
\end{cases}$$

**Explanation:** Stage updates automatically based on health thresholds. MVP starts at Moderate (stage 2). No Mild stage.

**Implemented in AnyLogic:**
- **Function:** `progressDisease()`, within `DiseaseProgression` event
- **Statechart:** ModerateDemo → SevereDementia → EndStage
- **Variable:** `dementiaStage`
- **Code logic:** If-else statements checking health thresholds

---

### Equation 2.3: Behavioural Symptoms

$$B(H) = \lfloor 10 \times (1 - H) \rfloor$$

**Explanation:** Integer scale 0-10. Symptoms worsen as health declines. Floor function rounds down to nearest integer.

**Implemented in AnyLogic:**
- **Function:** `updateCareNeeds()`
- **Variable:** `behaviouralSymptoms`
- **Code:** `behaviouralSymptoms = (int)(10 * (1.0 - healthStatus))`

---

### Equation 2.4: Care Needs (Hours Per Day)

$$N_C = \max\left(4, \; (4 + (1-H) \times 16) - P_h \times 0.02\right)$$

**Explanation:**
- Base: 4 hours/day minimum (safety margin)
- Scales to 20 hours max as health declines
- Professional care reduces burden (2% per hour/week)
- Final range: 4-20 hours/day

**Implemented in AnyLogic:**
- **Function:** `updateCareNeeds()`
- **Variable:** `careNeedsHours`
- **Code:**
```java
double baseCareNeeds = 4 + (1.0 - healthStatus) * 16;
double careNeedsReduction = professionalCareHours * 0.02;
careNeedsHours = Math.max(4, baseCareNeeds - careNeedsReduction);
```

---

### Equation 2.5: Effective Care Quality (Blended Family + Professional)

$$Q_E = \max\left(0.1, \; \min\left(1.0, \; \frac{Q_C \times H_f + 0.9 \times P_h}{H_f + P_h}\right)\right)$$

**Explanation:**
- $Q_C$: Caregiver care quality (variable with stress)
- $H_f$: Family hours/week (≈ $Q_C \times 40$)
- $P_h$: Professional care hours/week
- Professional quality: 0.9 (assumed high constant)
- Weighted average, constrained 0.1-1.0

**Implemented in AnyLogic:**
- **Function:** `calculateEffectiveCareQuality()`
- **Variable:** `effectiveCareQuality`
- **Code:**
```java
if (totalCareReceived > 0) {
    double familyContribution = myCaregiver.careQuality * careFromFamilyOnly;
    double professionalContribution = 0.9 * careFromProfessional;
    effectiveCareQuality = (familyContribution + professionalContribution) / totalCareReceived;
} else {
    effectiveCareQuality = (myCaregiver != null) ? myCaregiver.careQuality : 0.5;
}
effectiveCareQuality = Math.max(0.1, Math.min(1.0, effectiveCareQuality));
```

---

## 3. FEEDBACK LOOPS

### Caregiver Workload Updates

$$H_p^{(t+1)} \leftarrow N_C^{(t)}$$

**Explanation:** Patient's care needs directly update caregiver's workload each week, creating the feedback loop.

**Implemented in:** `updateCareNeeds()` function
- **Code:** `myCaregiver.workloadHoursPerDay = careNeedsHours`

---

### Bidirectional Feedback Cascade

$$\sigma_C \rightarrow Q_C \rightarrow Q_E \rightarrow H(t) \rightarrow N_C \rightarrow H_p \rightarrow \sigma_C$$

**Explanation:**
- Caregiver stress → reduces care quality
- Poor quality → worsens patient outcomes (faster health decline)
- Worse patient → increases care demands
- More demands → increases caregiver workload stress
- **Loop repeats (spirals down without intervention)**

**Implemented in:** Both agents' events running weekly with inter-agent coupling

---

## 4. INTERVENTION EFFECTS

### Professional Care Hours

$$P_h = \begin{cases}
20 & \text{if adult day program active} \\
20 + X & \text{if home care of X hours/week added} \\
0 & \text{if no professional services}
\end{cases}$$

**Typical values:**
- Adult day program: 20 hours/week (3 days × 6-7 hours)
- Home care: 10 hours/week (additive)
- Maximum combined: 30 hours/week

**Implemented in:**
- **Functions:** `enrollInAdultDayProgram()`, `startHomeCareServices(double hoursPerWeek)`
- **Variables:** `inAdultDayProgram`, `receivingHomeCare`, `professionalCareHours`
- **Triggered by:** Caregiver stress ≥ 2.7 (Crisis state)

---

### Stress Relief from Service

$$\sigma_C^{\text{after}} = \max(0, \; \sigma_C^{\text{before}} - \Delta\sigma)$$

Where $\Delta\sigma = 0.3$ (30% stress reduction, default parameter)

**Implemented in:**
- **Function:** `applyServiceRelief(double relieveAmount)`
- **Called from:** DES Sink block when service completes
- **Code:** `stressLevel = Math.max(0, stressLevel - relieveAmount)`

---

## 5. VARIABLE REFERENCE TABLE

| Mathematical Symbol | AnyLogic Variable | Type | Range | Units |
|--------|---|---|---|---|
| $\sigma_C$ | `stressLevel` | double | 0–3 | scale |
| $S_W$ | `workloadStress` | double | 0–3 | scale |
| $S_F$ | `financialStress` | double | 0–3 | scale |
| $S_S$ | `sleepStress` | double | 0–3 | scale |
| $Q_C$ | `careQuality` | double | 0.2–1.0 | scale |
| $C_S$ | `copingSkills` | double | 0–1 | scale |
| $H_p$ | `workloadHoursPerDay` | double | 0–24 | hrs/day |
| $I_m$ | `familyIncomeMonthly` | double | 0+ | CAD |
| $S_h$ | `sleepQualityHours` | double | 0–12 | hrs/night |
| $H(t)$ | `healthStatus` | double | 0–1 | scale |
| $B$ | `behaviouralSymptoms` | int | 0–10 | integer |
| $N_C$ | `careNeedsHours` | double | 4–20 | hrs/day |
| $Q_E$ | `effectiveCareQuality` | double | 0.1–1.0 | scale |
| $P_h$ | `professionalCareHours` | double | 0+ | hrs/week |
| $r_d$ | `progressionRate` | double | 0.015 | per week |

---

## 6. EVENTS AND TIMING

| Event | Trigger | Frequency | What It Does |
|-------|---------|-----------|--------------|
| `DiseaseProgression` | Time-based | Weekly | Updates patient health, symptoms, care needs |
| `StageTransition` | Time-based | Every 4 weeks | Monitors & logs dementia stage changes |
| `StressUpdate` | Time-based | Weekly | Recalculates caregiver stress & care quality |
| `ServiceRequest` | Dynamic | On-demand | Triggered when caregiver enters Crisis state |

---

---

**Document Type:** Technical Specification (Part 3 Interim Report)  

**Last Updated:** October 28, 2025  
**Team:** CMPT 394 - Team 5
