# Model Equations - V4

## Overview

This document explains the math for our agent-based model.

We are using a **weighted linear model** for stress, and this document shows our current equations as implemented in AnyLogic.

---

## 1. CAREGIVER AGENT EQUATIONS

### Equation 1.1: Workload Stress Component

Stress from the total hours of care per week (H_p):

- 0.0 if H_p < 10
- 0.5 if 10 ≤ H_p < 25
- 1.0 if 25 ≤ H_p < 40
- 1.5 if 40 ≤ H_p < 60
- 2.0 if 60 ≤ H_p < 90
- 2.5 if 90 ≤ H_p < 120
- 3.0 if H_p ≥ 120

**What It Measures:** Stress from the _total hours of care per week_.

**Why It Matters:** This is a main cause of tiredness. Our research (CIHI, 2025) shows caregivers provide **26 hours/week**, which fits into our "Low Stress" (0.5) bracket.

**Implemented in:** `workloadCalculation()` function in AnyLogic

---

### Equation 1.2: Financial Stress Component

Stress from weekly family income (I_w) with base costs (baseCost = $1500/week):

**incomeRatio = I_w / baseCost**

Then:

- 0.0 if incomeRatio ≥ 1.5 (comfortable)
- 0.3 if 1.0 ≤ incomeRatio < 1.5 (buffer)
- 0.6 if 0.8 ≤ incomeRatio < 1.0 (breaking even)
- 1.0 if 0.6 ≤ incomeRatio < 0.8 (moderate strain)
- 1.3 if 0.4 ≤ incomeRatio < 0.6 (high strain)
- 1.7 if incomeRatio < 0.4 (severe strain)
- 3.0 if incomeRatio < 0.15 (critical)

**What It Measures:** Stress from **weekly** family income adjusted by base costs.

**Why It Matters:** This models financial strain as a ratio. The stipend can be added directly to income:

**incomeThisWeek = familyWeeklyIncome + (stipendAmount if enableStipend)**

> **Note to Team:**
>
> This ratio-based approach is more realistic than fixed brackets.
>
> - Sask Dashboard shows average weekly earning of ~$1,275.
> - With baseCost = $1500, this gives an incomeRatio of 0.85 (moderate strain, stress = 1.0).
> - This validates that a "normal" single-income family in our model starts with moderate financial stress.

**Implemented in:** `weeklyIncomeCalculation()` function in AnyLogic

---

### Equation 1.3: Sleep Quality Stress Component

Stress from total hours of sleep per week (S_h):

- 0.0 if S_h ≥ 49
- 0.5 if 42 ≤ S_h < 49
- 1.0 if 35 ≤ S_h < 42
- 1.5 if 28 ≤ S_h < 35
- 2.0 if 21 ≤ S_h < 28
- 2.5 if 14 ≤ S_h < 21
- 3.0 if S_h < 14

**What It Measures:** Stress from the _total hours of sleep per week_.

**Why It Matters:** Sleep deprivation is a critical stressor. We've added a "no stress" category (0.0) for caregivers who get plenty of sleep.

**Implemented in:** `sleepQualityCalculation()` function in AnyLogic

> **Note to Team:**
>
> These numbers are daily numbers multiplied by 7.
>
> - **No Stress (≥ 49 /wk):** 7+ hours/night. Healthy amount.
> - **Low Stress (42-49 /wk):** 6 to 7 hours/night. "Acceptable but not great."
> - **Moderate Stress (35-42 /wk):** 5 to 6 hours/night. "Getting impacted."
> - **High Stress (< 35 /wk):** Less than 5 hours/night. Crisis range.

---

### Equation 1.4: Composite Caregiver Stress Level (ACTUAL IMPLEMENTATION)

Total caregiver stress using **weighted sum**:

**stressLevel = (w_W × S_W + w_F × S_F + w_S × S_S) / (w_W + w_F + w_S)**

**Where:**

- w_W = weight for workload stress (default = 0.33)
- w_F = weight for financial stress (default = 0.33)
- w_S = weight for sleep stress (default = .34)

By default (all weights = 1.0), this simplifies to:

**stressLevel = (S_W + S_F + S_S) / 1**

**What It Measures:** The _total_ caregiver stress.

**Why It Matters:** This is our core stress equation. It's linear and flexible. We can adjust weights later for sensitivity analysis.

> ** Implementation Note:**
>
> - **Current behaviour:** All three stressors are equally weighted.
> - **Future flexibility:** We can change w_W, w_F, w_S to model different priorities.
> - **Example:** If sleep is more critical, set w_S = 0.5, w_W = 0.3, w_F = 0.2 (weights sum to 1.0).
> - **Always normalizes** by dividing by the sum of weights, so final stressLevel stays 0-3.

**Implemented in:** `calculateStressLevels()` function in AnyLogic

---

### Equation 1.5: Care Quality Delivered by Caregiver

Quality of care the caregiver provides:

**normalizedStress = stressLevel / 3.0**

**stressImpact = normalizedStress × 0.5**

**copingBonus = copingSkills × 0.2**

**careQuality = 0.8 - stressImpact + copingBonus**

**careQuality = max(0.2, min(1.0, careQuality))**

**If formal services active:** careQuality += 0.1

**What It Measures:** The quality of care the caregiver provides.

**Why It Matters:** Links caregiver stress directly to patient health. As stress goes up, care quality goes down. Professional services can offset stress impacts.

> **How It Works:**
>
> - **Start at 0.8:** Base care quality (decent but not perfect)
> - **Subtract stress impact:** For every 3.0 stress points, loses 0.5 points quality
> - **Add coping bonus:** For every 1.0 coping skill point, gains 0.2 quality
> - **Add service bonus:** If in professional care, gain 0.1 quality
> - **Clamp result:** Final quality always between 0.2 (very poor) and 1.0 (perfect)

**Example:**

- High stress (σ = 2.4): normalizedStress = 0.8, stressImpact = 0.4
- Good coping (C_S = 0.6): copingBonus = 0.12
- No services: careQuality = 0.8 - 0.4 + 0.12 = 0.52 (moderate quality)

**Implemented in:** `calculateCareQuality()` function in AnyLogic

---

## 2. PATIENT WITH DEMENTIA EQUATIONS

### Equation 2.1: Disease Progression (Weekly Decline)

Patient's weekly health decline:

**weeklyDecline = (progressionRate / 100) × (1.0 - effectiveCareQuality × 0.5)**

**healthStatus(t+1) = max(0, healthStatus(t) - weeklyDecline)**

**What It Measures:** The patient's weekly health decline.

**Why It Matters:** Core "engine" of patient decline. Based on "predictable transitions" that can be slowed by high-quality care. Quality of care slows (but doesn't stop) decline.

> **How It Works:**
>
> - **Base decline:** progressionRate / 100 (typically 1.5% per week)
> - **Care multiplier:** (1.0 - effectiveCareQuality × 0.5)
>   - At perfect care (Q_E = 1.0): multiplier = 0.5 (50% of decline)
>   - At poor care (Q_E = 0.1): multiplier = 0.95 (95% of decline)
> - **Max floor:** Health never goes below 0

**Example:**

- Progression rate = 1.5
- Effective care quality = 0.7
- weeklyDecline = (1.5/100) × (1.0 - 0.7 × 0.5) = 0.015 × 0.65 = 0.0098 (0.98% actual decline)

**Implemented in:** Patient's `updateCareNeeds()` function

---

### Equation 2.2: Dementia Stage Assignment

Patient's clinical stage based on health variable:

- Moderate if H ≥ 0.35
- Severe if 0.15 ≤ H < 0.35
- End-stage if H < 0.15

**What It Measures:** The patient's clinical stage.

**Why It Matters:** Drives care needs and triggers statechart transitions. Aligns with "evolving long-term process" of caregiving.

**Implemented in:** `progressDisease()` function

---

### Equation 2.3: Behavioural Symptoms

Severity of behavioural symptoms (0-10) based on health:

**behaviouralSymptoms = floor(10 × (1 - healthStatus))**

**What It Measures:** Severity of behavioural symptoms.

**Why It Matters:** Major feedback loop that affects caregiver stress through sleep disruption.

**Implemented in:** Patient's updateCareNeeds() function

---

### Equation 2.4: Care Needs (Hours Per Week) - UPDATED

How many hours of care patient needs per week:

**N_C = max(28, (28 + (1 - H) × 112) - P_h)**

**What It Measures:** Hours of care needed _per week_.

**Why It Matters:** Primary driver of caregiver workload. Updated to weekly units for simplicity.

> **How It Works:**
>
> - `28`: Minimum (4 hours/day × 7 days/week).
> - `112`: Range. Max care is 20 hours/day (140/week). Additional care = 140 - 28 = 112.
> - `28 + (1-H) × 112`: Total care needed.
>   - If healthy (H=1): 28 hours (just daily living support)
>   - If end-stage (H=0): 140 hours (full-time intensive care, ~20 hrs/day)
> - **Intervention:** Subtract `professionalCareHoursPerWeek` (P_h) directly.
> - `max(28, ...)`: Floor ensuring need never drops below 28 hours/week.

**Example:**

- Patient is moderate (H = 0.65): careNeeds = 28 + 0.35 × 112 = 28 + 39.2 = 67.2 hours/week
- With day care (P_h = 20): effective caregiver workload = 67.2 - 20 = 47.2 hours/week

**Implemented in:** Patient's `updateCareNeeds()` function

---

### Equation 2.5: Effective Care Quality (Blended)

Total quality of care patient receives (blending family and professional):

**effectiveCareQuality = max(0.1, min(1.0, (careQuality × workloadHours + 0.9 × professionalHours) / (workloadHours + professionalHours)))**

**What It Measures:** _Total_ quality of care from both sources.

**Why It Matters:** Shows _why_ our intervention (Adult Day Homes, P_h) works. It not only reduces caregiver workload but also boosts overall care quality, slowing disease progression.

> **How It Works:**
>
> - **Family care contribution:** careQuality × workloadHours
> - **Professional care contribution:** 0.9 × professionalHours (assumes 90% quality)
> - **Weighted average:** Total hours in denominator
> - **Bounds:** Always between 0.1 (minimal) and 1.0 (excellent)

**Example:**

- Caregiver quality = 0.6, workload hours = 40
- Professional hours = 20 (day care)
- effectiveCareQuality = (0.6 × 40 + 0.9 × 20) / (40 + 20) = (24 + 18) / 60 = 0.7

**Implemented in:** Caregiver's `calculateCareQuality()` function

---

## 3. THE FEEDBACK LOOP: How Everything Connects

### The Downward Spiral (Core of Our Model)

Flow: σ_C → Q_C → Q_E → H(t) → N_C → H_p → σ_C

**The Flow:**

1. **Caregiver stress (σ_C) is HIGH.** (45% of caregivers)
2. ↓ **Care quality (Q_C) DROPS** (stress increases, quality decreases)
3. ↓ **Effective quality (Q_E) DROPS** (no professional help to compensate)
4. ↓ **Patient health (H(t)) DECLINES FASTER** (poor care speeds decline)
5. ↓ **Care needs (N_C) INCREASE** (sicker patient needs more hours)
6. ↓ **Caregiver workload (H_p) INCREASES**
7. ↓ **Behavioral symptoms increase** (patient sicker → worse behavior)
8. ↓ **Sleep quality DROPS** (symptoms disrupt caregiver sleep)
9. ↓ **Caregiver stress (σ_C) GOES UP**
10. **LOOP REPEATS → Spiral downward!**

---

---

## 4. IMPLEMENTATION DETAILS (AnyLogic Functions)

### Function: weeklyIncomeCalculation()

```java
// INPUT: familyWeeklyIncome (could be $0-$3000+)
// OUTPUT: financialStress (will be 0.0-3.0)

double baseCost = 1500.0; // Realistic weekly expenses for dementia care household

double incomeThisWeek = familyWeeklyIncome;

// Add stipend if enabled
if (main.enableStipend) {
    incomeThisWeek += main.stipendAmount; // e.g., +$200/week
}

// Calculate income ratio (buffering effect)
double incomeRatio = incomeThisWeek / baseCost;

// Map ratio to financial stress (0-3 scale, bounded)
if (incomeRatio >= 1.5) {
    financialStress = 0.0; // Income well above expenses
} else if (incomeRatio >= 1.0) {
    financialStress = 0.3; // Comfortable buffer
} else if (incomeRatio >= 0.8) {
    financialStress = 0.6; // Breaking even
} else if (incomeRatio >= 0.6) {
    financialStress = 1.0; // Moderate strain
} else if (incomeRatio >= 0.4) {
    financialStress = 1.3; // High strain
} else if (incomeRatio >= 0.15) {
    financialStress = 1.7; // Severe strain
} else {
    financialStress = 3.0; // Critical (clamped at 3.0)
}
```

### Function: workloadCalculation()

```java
// INPUT: workloadHoursPerWeek (could be 0-140)
// OUTPUT: workloadStress (will be 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, or 3.0)

if (workloadHoursPerWeek < 10) {
    workloadStress = 0.0;
} else if (workloadHoursPerWeek < 25) {
    workloadStress = 0.5;
} else if (workloadHoursPerWeek < 40) {
    workloadStress = 1.0;
} else if (workloadHoursPerWeek < 60) {
    workloadStress = 1.5;
} else if (workloadHoursPerWeek < 90) {
    workloadStress = 2.0;
} else if (workloadHoursPerWeek < 120) {
    workloadStress = 2.5;
} else {
    workloadStress = 3.0;
}
```

### Function: sleepQualityCalculation()

```java
// INPUT: sleepQualityHourperWeek (could be 0-56)
// OUTPUT: sleepStress (will be 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, or 3.0)

if (sleepQualityHourperWeek >= 49) {
    sleepStress = 0.0;
} else if (sleepQualityHourperWeek >= 42) {
    sleepStress = 0.5;
} else if (sleepQualityHourperWeek >= 35) {
    sleepStress = 1.0;
} else if (sleepQualityHourperWeek >= 28) {
    sleepStress = 1.5;
} else if (sleepQualityHourperWeek >= 21) {
    sleepStress = 2.0;
} else if (sleepQualityHourperWeek >= 14) {
    sleepStress = 2.5;
} else {
    sleepStress = 3.0;
}
```

### Function: calculateStressLevels() (ACTUAL WEIGHTED VERSION)

```java
// Normalize quadratic stress to 0-1 scale
double normalizedStress = stressLevel / 3.0; // Max is 3.0

// Calculate care quality
double stressImpact = normalizedStress * 0.5;
double copingBonus = copingSkillsBoostAmount * 0.2;

careQuality = 0.8 - stressImpact + copingBonus;

if (inService) {
    careQuality += 0.1; // Service relief bonus
}

// Clamp to valid range
careQuality = Math.max(0.2, Math.min(1.0, careQuality));
```

### Function: calculateCompositeStress() (WEIGHTED)

```java
// Calculate weighted stress level
stressLevel = (w_W * workloadStress + w_F * financialStress + w_S * sleepStress)
              / (w_W + w_F + w_S);

// Default: w_W = w_F = w_S = 1.0, so this becomes:
// stressLevel = (workloadStress + financialStress + sleepStress) / 3
```

---

## 6. VARIABLE REFERENCE TABLE (Updated)

| Symbol | AnyLogic Variable            | Type   | Range   | Meaning                                     |
| ------ | ---------------------------- | ------ | ------- | ------------------------------------------- |
| σ_C    | stressLevel                  | double | 0–3     | Overall caregiver stress (weighted average) |
| S_W    | workloadStress               | double | 0–3.0   | Stress from care hours per week             |
| S_F    | financialStress              | double | 0–3.0   | Stress from income ratio vs. costs          |
| S_S    | sleepStress                  | double | 0–3.0   | Stress from weekly sleep hours              |
| Q_C    | careQuality                  | double | 0.2–1.0 | Quality of family care                      |
| C_S    | copingSkills                 | double | 0–1     | Caregiver's coping ability                  |
| H_p    | workloadHoursPerWeek         | double | 0+      | Hours family spends on care per week        |
| I_w    | familyIncomeWeekly           | double | 0+      | Weekly family income                        |
| S_h    | sleepQualityHourperWeek      | double | 0+      | Hours of sleep per week                     |
| H(t)   | healthStatus                 | double | 0–1     | Patient's health (1=healthy, 0=end-stage)   |
| B      | behaviouralSymptoms          | int    | 0–10    | Behavioral symptom severity                 |
| N_C    | careNeedsHoursPerWeek        | double | 28–140  | Hours of care needed per week               |
| Q_E    | effectiveCareQuality         | double | 0.1–1.0 | Combined family+prof. care quality          |
| P_h    | professionalCareHoursPerWeek | double | 0+      | Hours of professional care per week         |
| r_d    | progressionRate              | double | 1.5     | Disease decline rate (% per week)           |
| w_W    | weightWorkload               | double | 0-1     | Weight for workload component               |
| w_F    | weightFinancial              | double | 0-1     | Weight for financial component              |
| w_S    | weightSleep                  | double | 0-1     | Weight for sleep component                  |

---


## 6. SUMMARY: How the Model Works

1.  **Patient health ($H(t)$)** determines their **Care Needs ($N_C$)** (Eq 2.4).
2.  **Care Needs ($N_C$)** (minus professional help) become the caregiver's **Workload ($H_p$)**.
3.  **Workload ($H_p$)**, **Finances ($S_F$)**, and **Sleep ($S_S$)** are **averaged** to create **Total Stress ($\sigma_C$)** (Eq 1.4).
4.  **Total Stress ($\sigma_C$)** *linearly* damages the caregiver's **Care Quality ($Q_C$)** (Eq 1.5).
5.  **Caregiver Quality ($Q_C$)** and **Professional Care ($P_h$)** blend to create **Effective Care ($Q_E$)** (Eq 2.5).
6.  **Effective Care ($Q_E$)** slows the **Patient's Health Decline ($H(t+1)$)** (Eq 2.1).
7.  **Loop repeats weekly.**

---

## 7. Project References and Source Material

These are the core documents our team is using as a foundation for this model's concepts and justifications.

* **Saskatchewan Dashboard (2025) - Average Weekly Earnings**
    * **URL:** https://dashboard.saskatchewan.ca/business-economy/employment-labour-market/average-weekly-earnings
    * **What it supports:** Provides the baseline average weekly earning of **$1,275/week**, which we use to model our `SINGLE_INCOME`, `DUAL_INCOME`, and `RETIRED` agent parameters.

* **CCCE (2024) - Caring in Canada Report**
    * **Source:** Canadian Centre for Caregiving Excellence (CCCE)
    * **URL:** https://canadiancaregiving.org/wp-content/uploads/2024/06/CCCE_Caring-in-Canada.pdf
    * **What it supports:** **22% of caregivers spend at least $1,000/month ($230/week)** on out-of-pocket expenses. Also, **51% of caregivers in Saskatchewan** reported financial hardship.

* **A Place for Mom (2024) - In-Home Dementia Care Costs**
    * **URL:** https://www.aplaceformom.com/caregiver-resources/articles/cost-of-dementia-care
    * **What it supports:** Provides an average cost for part-time (15 hrs/wk) hired care of **~$495/week** (`$2145/month`), which we use to justify our "Severe" stage costs.

* **Nova Scotia (2025) - Caregiver Benefit Program**
    * **Source:** Government of Nova Scotia
    * **URL:** https://novascotia.ca/dhw/ccs/caregiver-benefit.asp
    * **What it supports:** Justifies our stipend intervention. This is a real-world Canadian program that provides a direct **$400/month** stipend to low-income caregivers.

* **CIHI (2025) - Unpaid Caregiver Challenges and Supports**
    * **Source:** Canadian Institute for Health Information (CIHI)
    * **URL:** https://www.cihi.ca/en/dementia-in-canada/unpaid-caregiver-challenges-and-supports
    * **What it supports:**
        * Dementia caregivers provide **26 hours/week** (vs 17 for other seniors).
        * **45% experience distress** (vs 26% for other caregivers).
        * **$1.4 billion** total out-of-pocket costs for Canadian caregivers.
        * **Behavioural/verbal aggression increases distress by 1.6×**.

* **Chambers et al. (2005) - Research on Alzheimer's Caregiving in Canada**
    * **Source:** Health Promotion and Chronic Disease Prevention in Canada (Peer-reviewed)
    * **URL:** https://www.canada.ca/en/public-health/services/reports-publications/health-promotion-chronic-disease-prevention-canada-research-policy-practice/vol-25-no-3-2004/research-on-alzheimercaregiving-canada.html
    * **What it supports:**
        * **63 hours/month** informal assistance from primary caregivers.
        * Family members provide **75-85% of care** for frail Canadian seniors.
        * Caregiving is an **evolving long-term process** with predictable transitions.

* **TERM PROJECT_Dementia and Caregivers_PART 2_OCTOBER 16_2025.pdf**
    * **Source:** Our Team's Project Proposal
    * **What it supports:** The core model concept: Stressors $\rightarrow$ Caregiver Stress Level $\rightarrow$ Quality of Care $\rightarrow$ Disease Progression. It also defines our "what-if" scenarios (interventions).

* **YujiePei_Report_CMPT858_CaregiverDistress_OriginalVersion.pdf**
    * **Source:** Example CMPT 858 Project
    * **What it supports:** Serves as an example to follow for using equations, statecharts for dementia progression, and for planning our sensitivity analysis.

---

**Last Updated:** November 1, 2025
