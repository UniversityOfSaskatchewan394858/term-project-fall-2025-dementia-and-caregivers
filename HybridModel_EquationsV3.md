# Model Equations - V3

## Overview

This document explains the math for our agent-based model. This version has been updated after our meeting to use **weekly** time units for all caregiver stress variables. This makes our model more realistic and allows us to directly use the data from our research (like the CIHI and Sask Dashboard sources).

We are using a **linear model** for stress, and this document shows our current equations, plus some new proposals for our team to discuss.

This file also includes our next big step (Section 4), which is planning the **Discrete-Event Simulation (DES)** part of our hybrid model.

---

## 1. CAREGIVER AGENT EQUATIONS

### Equation 1.1: Workload Stress Component

$$S_W = \begin{cases}
0.5 & \text{if } H_p < 25 \\
1.5 & \text{if } 25 \leq H_p < 30 \\
3.0 & \text{if } H_p \geq 30
\end{cases}$$

**What It Measures:** Stress from the *total hours of care per week* ($H_p$).

**Why It Matters:** This is a main cause of tiredness. Our research (CIHI, 2025) shows caregivers provide **26 hours/week**, which fits perfectly into our "Moderate Stress" (1.5) bracket.

**Implemented in:** `calculateStressLevels()`

---

### Equation 1.2: Financial Stress Component (Our Current Version)

$$S_F = \begin{cases}
3.0 & \text{if } I_w < 800 \\
1.5 & \text{if } 800 \leq I_w < 1300 \\
0.5 & \text{if } 1300 \leq I_w < 1900 \\ 
0.0 & \text{if } I_w \geq 1900
\end{cases}$$

**What It Measures:** Stress from **weekly** family income ($I_w$).

**Why It Matters:** This models financial strain. We've also added a "no stress" category (0.0) for high-income families.

> ** Note to Team:**
>
> These new weekly numbers are *much* more realistic for Saskatchewan.
>
> * Our Sask Dashboard source shows the **average weekly earning is $1,300.**
> * This number now falls perfectly into our "Moderate Stress" (1.5) bracket.
> * This is great justification. It means a "normal" single-income family in our model will start with moderate financial stress.
> * We can still use the `incomeType` parameter (Single, Dual, Retired) to set the `familyIncomeWeekly` for each agent IF we go that route.

**New Proposed Equation:**

$$S_F = \begin{cases}
3.0 & \text{if } \text{StrainRatio} > 0.25 \\
1.5 & \text{if } 0.10 < \text{StrainRatio} \leq 0.25 \\
0.5 & \text{if } \text{StrainRatio} \leq 0.10
\end{cases}$$

> ** Proposal for a Better Equation (Later in the project if time allows!!!):**
>
> I think we should switch from these fixed income brackets to a **"Strain Ratio"** (`Costs / Income`), as shown above.
>
> **Why this is better:**
> 1.  **It's more realistic:** It measures the *percentage* of income someone spends on care. A $150-300/wk cost is a crisis for a low-income family (37% strain) but manageable for a high-income one (12% strain).
> 2.  **It makes our stipend idea work perfectly:** To test the stipend, we just add it to the income. This will clearly lower the `StrainRatio` and show a clear drop in stress. We have a source that Nova Scotia already does this.

---

### Equation 1.3: Sleep Quality Stress Component

$$S_S = \begin{cases}
3.0 & \text{if } S_h < 28 \\ 
1.5 & \text{if } 28 \leq S_h < 42 \\
0.5 & \text{if } 42 \leq S_h < 49 \\
0.0 & \text{if } S_h \geq 49
\end{cases}$$

**What It Measures:** Stress from the *total hours of sleep per week* ($S_h$).

**Why It Matters:** Sleep deprivation is a critical stressor. We've added a "no stress" category (0.0) for caregivers who get plenty of sleep.

**Implemented in:** `calculateStressLevels()`

> ** Note to Team:**
>
> These numbers are just the daily numbers multiplied by 7.
>
> * **High Stress (`< 28` /wk):** This is less than 4 hours/night. This is the crisis level.
> * **Moderate Stress (`28-42` /wk):** This is 4 to 6 hours/night. This is the "not coping" range.
> * **Low Stress (`42-49` /wk):** This is 6 to 7 hours/night. This is "acceptable but not great."
> * **No Stress (`>= 49` /wk):** This is 7+ hours/night, which is a healthy amount.

---

### Equation 1.4: Composite Caregiver Stress Level (Our Current Version)

$$\sigma_C = \frac{S_W + S_F + S_S}{3}$$

**What It Measures:** The *total* caregiver stress, by averaging the three components.

**Why It Matters:** This is our core stress equation. It's simple, linear, and easy to explain.

> **💡 Note to Team (Our Current Method):**
>
> * **What this means:** We are assuming that Workload, Finances, and Sleep are all **equally important** (each one is 33.3% of the total stress).
> * **How it works:** The equation adds the three stress scores (which are 0, 0.5, 1.5, or 3.0) and divides by 3. The final `stressLevel` ($\sigma_C$) will be a number between 0 and 3.

**New Proposed Equation:**

$$\sigma_C = (w_W \cdot S_W) + (w_F \cdot S_F) + (w_S \cdot S_S)$$

> ** Proposal for a Better Equation (Based on Yujie's Report - Later in the project if time allows!):**
>
> Instead of the simple average above, I propose we use a **Weighted Sum**, just like the reference paper (`YujiePei_Report`) does [cite: page 11-12].
>
> * **What this means:** We can *decide* how important each stressor is. The weights ($w$) must add up to 1.0.
> * **Example Weights:** We could say Sleep is most important (50%), Workload is second (30%), and Finances last (20%).
>   * $w_S$ (Sleep) = **0.5**
>   * $w_W$ (Workload) = **0.3**
>   * $w_F$ (Financial) = **0.2**
>
> **Why this is better:** It's more realistic (bad sleep is probably worse than bad finances) and it's *still* a simple linear model. It also lets us do a Sensitivity Analysis for our final report by changing the weights.

---

### Equation 1.5: Care Quality Delivered by Caregiver

$$Q_C = \max(0.2, \min(1.0, \; 0.8 - \frac{\sigma_C}{3} \times 0.5 + C_S \times 0.2))$$

**What It Measures:** The quality of care the caregiver provides.

**Why It Matters:** This links caregiver stress directly to patient health. As stress ($\sigma_C$) goes up, care quality ($Q_C$) goes down.

> ** Note on `max(min(...))` notation:**
>
> This is a "clamp" to keep our value in a specific range.
> * `min(1.0, ...)` means "Give me the **smaller** of 1.0 or the result." (This sets a **max limit** of 1.0).
> * `max(0.2, ...)` means "Give me the **bigger** of 0.2 or that result." (This sets a **min limit** of 0.2).
> * This just forces the final answer to always be between 0.2 (very poor care) and 1.0 (perfect care).

> **💡 Note on the `(sigma_C / 3)` term:**
>
> * **What it does:** This "normalizes" our stress score. It takes the `stressLevel` (which is on a 0-3 scale) and turns it into a 0-1 percentage.
> * **Example:** A max stress of `3.0` becomes an impact of `3.0 / 3 = 1.0` (or 100%). A moderate stress of `1.5` becomes an impact of `1.5 / 3 = 0.5` (or 50%).
> * This is the correct way to use our stress score in this equation.

---

## 2. PATIENT WITH DEMENTIA EQUATIONS

### Equation 2.1: Disease Progression (Weekly Decline)

$$H(t+1) = \max\left(0, \; H(t) - \frac{r_d \times (1 - Q_E \times 0.5)}{100}\right)$$

**What It Measures:** The patient's weekly health decline.

**Why It Matters:** This is the core "engine" of the patient's decline. It's based on the concept of "predictable transitions" that can be slowed by high-quality care.

> ** Note on `max(0, ...)` notation:**
>
> This is a trick to set a **"floor"** or a **minimum value** of 0.
> * `max(0, ...)` means "Give me the **bigger** of 0 or the calculated result."
> * This simply stops the patient's health from ever going below 0.

> ** Note on the `/ 100` division:**
>
> The `/ 100` is here to convert our `r_d` (progression rate) from a percentage into a decimal.
> * In our Variable Table (Section 5), we've set `r_d = 1.5`.
> * This equation does `1.5 / 100` to get `0.015`, which is the 1.5% weekly decline we want.

---

### Equation 2.2: Dementia Stage Assignment

$$\text{Stage}(H) = \begin{cases}
\text{Moderate} & \text{if } H \geq 0.35 \\
\text{Severe} & \text{if } 0.15 \leq H < 0.35 \\
\text{End-stage} & \text{if } H < 0.15
\end{cases}$$

**What It Measures:** The patient's clinical stage based on their health variable.

**Why It Matters:** Drives care needs and triggers statechart transitions. This aligns with the "evolving long-term process" of caregiving.

**Implemented in:** `progressDisease()`

---

### Equation 2.3: Behavioral Symptoms

$$B(H) = \lfloor 10 \times (1 - H) \rfloor$$

**What It Measures:** Severity of behavioral symptoms (0-10) based on health.

**Why It Matters:** This is a major (and currently *missing*) feedback loop. We need to use this variable to influence the caregiver's stress.

> **💡 Proposal for Our Next Discussion:**
>
> Right now, our model calculates $B(H)$ but never *uses* it. This is a huge missed opportunity to use our research.
>
> I propose we create a direct link. We can make the patient's symptoms directly reduce the caregiver's sleep *before* we calculate sleep stress.
>
> **How to Implement in AnyLogic (Java):**
> ```java
> // 1. In Patient's updateCareNeeds() function
> this.behaviouralSymptoms = (int)Math.floor(10 * (1 - this.healthStatus));
> 
> // 2. In Caregiver's calculateStressLevels() function
> double baseSleepPerWeek = 56.0; // 8 hours * 7 days
> double sleepLossPerWeek = myPatient.behaviouralSymptoms * 2.8;
> this.sleepQualityHourperWeek = Math.max(0, baseSleepPerWeek - sleepLossPerWeek);
> 
> // 3. NOW calculate sleep stress using this new number
> if (this.sleepQualityHourperWeek < 28) {
>     this.sleepStress = 3.0;
> } else if (this.sleepQualityHourperWeek < 42) {
>     this.sleepStress = 1.5;
> } else if (this.sleepQualityHourperWeek < 49) {
>     this.sleepStress = 0.5;
> } else {
>     this.sleepStress = 0.0;
> }
> ```
> **Why?**
> * This models that a patient with 10/10 symptoms ($B=10$) reduces caregiver sleep by 28 hours/week (from 56 down to 28).
> * This would cause their sleep stress to jump from **No Stress (0.0)** to **High Stress (3.0)**.
> * This is **strongly supported by our CIHI (2025) source**, which says "Behavioural/verbal aggression increases distress by 1.6×". This equation *is* that link.

---

### Equation 2.4: Care Needs (Hours Per Week) - **UPDATED**

$$N_C = \max\left(28, \; (28 + (1-H) \times 112) - P_h\right)$$

**What It Measures:** How many hours of care the patient needs *per week*.

**Why It Matters:** This is the primary driver of caregiver workload ($H_p$). We've updated this equation to be fully in "weekly" units, so it's simpler and works with our other variables.

> **💡 Note to Team: This equation is now fixed.**
>
> The old one was in "hours per day" and had a confusing "0.02" number. This new one is much cleaner.
>
> * **How it works:**
>     * `28`: This is the new minimum (4 hours/day * 7 days/week).
>     * `112`: This is the new range. The *max* care was 20 hours/day (140/week). So the *additional* care on top of the minimum is `140 - 28 = 112`.
>     * `28 + (1-H) * 112`: This calculates the total needed. If patient is healthy (`H=1`), it's 28 hours. If patient is at end-stage (`H=0`), it's 140 hours.
> * **Intervention:** We can now subtract the `professionalCareHoursPerWeek` ($P_h$) *directly*.
> * **`max(28, ...)`:** This is the "floor," making sure the need never drops below 28 hours/week.

---

### Equation 2.5: Effective Care Quality (Blended)(**NOT USED FOR NOW**)

$$Q_E = \max\left(0.1, \; \min\left(1.0, \; \frac{Q_C \times H_f + 0.9 \times P_h}{H_f + P_h}\right)\right)$$

**What It Measures:** The *total* quality of care the patient receives, blending family and professional care.

**Why It Matters:** This equation shows *why* our intervention (Adult Day Homes, $P_h$) works. It not only reduces caregiver workload (Eq 2.4) but also *simultaneously* boosts the overall care quality, slowing the disease (Eq 2.1).

---

## 3. THE FEEDBACK LOOP: How Everything Connects

### The Downward Spiral (The Core of Our Model)

$$\sigma_C \rightarrow Q_C \rightarrow Q_E \rightarrow H(t) \rightarrow N_C \rightarrow H_p \rightarrow \sigma_C$$

**The Flow:**

1.  **Caregiver stress ($\sigma_C$) is HIGH.** (45% of caregivers)
2.  $\downarrow$ **Care quality ($Q_C$) DROPS** (as stress goes up, quality goes down, Eq 1.5).
3.  $\downarrow$ **Effective quality ($Q_E$) DROPS** (no professional help to compensate).
4.  $\downarrow$ **Patient health ($H(t)$) DECLINES FASTER** (poor care speeds up decline, Eq 2.1).
5.  $\downarrow$ **Care needs ($N_C$) INCREASE** (sicker patient needs more hours, Eq 2.4).
6.  $\downarrow$ **Caregiver workload ($H_p$) INCREASES.**
7.  $\downarrow$ **Caregiver stress ($\sigma_C$) GOES UP.**
8.  **LOOP REPEATS $\rightarrow$ Spiral downward!**

> ** Note to Team:**
>
> This is our core story. It's clean, linear, and easy to explain. This is *good* for our model for now, as it will clearly show *why* interventions are so critical. Our interventions (day home, stipend) will break this loop, and the results should be very clear.

---

## 4. NEXT STEPS: The Discrete-Event Simulation (DES) Model

> ** Note to Team: This is our next big task.**
>
> These equations are our Agent-Based (ABM) model. Now we have to build the "Hybrid" part, which is the Discrete-Event Simulation (DES).
>
> **1. What it is:**
> We will create a new agent, `HealthcareProvider`. Inside this agent, we will use blocks from the "Process Modeling Library" to build a flowchart. The most important block will be a **`ResourcePool`** called **`dayCareSlots`**. This will represent the limited number of "beds" or "spots" at an adult day care center (e.g., set the capacity to 20).
>
> **2. How it Links (Patient Mobility):**
> The `PatientWithDem` agent will get a new statechart for their *physical location*, just like in Yujie's report (Fig 2.2) [cite: page 8]. This statechart will have states like `AtPatientsHome` and `AtDayCareCentre`.
>
> **3. How it Links (Caregiver Support):**
> This is the key. When our `Caregiver` agent's `stressLevel` gets too high, they enter the `SeekingSupport` state. The "Entry Action" for this state will be code that tries to get help from the DES model.
>
> **Example Code in `SeekingSupport`:**
> ```java
> main.healthcareProvider.dayCareSlots.seize(1);
> ```
>
> **4. The "So What?":**
> If there is a free spot in the `dayCareSlots` pool, the caregiver "seizes" it. This will trigger a transition in the *patient's* statechart, moving them from `AtHome` to `AtDayCare`. This will then set the `professionalCareHoursPerWeek` ($P_h$) variable to something like `20`, which will finally give the caregiver relief.
>
> **But if the `dayCareSlots` are all full**, the caregiver is stuck in the `seize` block. They have to *wait in a queue*. This is the bottleneck. While they wait, they stay in the `SeekingSupport` state, their stress remains high, and the patient's health continues to decline.

---

## How to Implement This As of Now (Parameters vs. Variables)

> ** Note to Team: This is how we build the model in AnyLogic currently.**
>
> This explains what the difference is between a "Parameter" and a "Variable" and which ones we need to create.

### What is a **Parameter**?
* A **Parameter** is a **Setting**.
* We set its value *before* the simulation starts (e.g., `1.5`).
* It **does not change** while the model is running.
* **Think of it like:** The "Difficulty" setting in a game.
* **We use these for:** Our "what-if" interventions (like the stipend) and our main assumptions.

### What is a **Variable**?
* A **Variable** is a **Calculated Result**.
* Its value is **always changing** every week as our equations run.
* **Think of it like:** Your "Health Bar" or "Score" in a game.
* **We use these for:** The values we want to measure (like `stressLevel` and `healthStatus`).

---

### Our Project's Parameters (Settings)

**In `PatientWithDem`:**
* `progressionRate` ($r_d$): Set to `1.5`
* `professionalCareHoursPerWeek` ($P_h$): This is our **intervention #1**. We'll set it to `0` for the baseline, and `20` for the "day care" scenario.
* `outOfPocketCostsWeekly`: (For the "Strain Ratio" proposal). We'd set this based on the patient's stage.

**In `Caregiver`:**
* `copingSkills` ($C_S$): Set to a value like `0.5`.
* `stipendWeeklyAmount`: This is our **intervention #2**. We'll set it to `0` or `125`.
* `incomeType`: (For the "Strain Ratio" proposal). We'll set this to `SINGLE_INCOME`, `DUAL_INCOME`, or `RETIRED`.

### Our Project's Variables (Calculated Results)

**In `PatientWithDem`:**
* `healthStatus` ($H(t)$): **(MAIN PATIENT VARIABLE)** Changes every week based on Eq 2.1.
* `behaviouralSymptoms` ($B$): Changes every week based on health.
* `careNeedsHoursPerWeek` ($N_C$): Changes every week based on health.
* `effectiveCareQuality` ($Q_E$): Changes every week based on the caregiver's `careQuality`.

**In `Caregiver`:**
* `familyIncomeWeekly` ($I_w$): Set *once* at the start (based on the `incomeType` parameter).
* `workloadHoursPerWeek` ($H_p$): Changes every week (gets its value from the patient's $N_C$).
* `sleepQualityHourperWeek` ($S_h$): Changes every week (gets its value from the patient's $B$).
* `workloadStress` ($S_W$): Calculated every week.
* `financialStress` ($S_F$): Calculated every week.
* `sleepStress` ($S_S$): Calculated every week.
* `stressLevel` ($\sigma_C$): **(MAIN CAREGIVER VARIABLE)** Calculated every week based on Eq 1.4.
* `careQuality` ($Q_C$): **(MAIN "BRIDGE" VARIABLE)** Calculated every week based on Eq 1.5.

---

## 5. VARIABLE REFERENCE TABLE (Updated)

| Symbol | AnyLogic Variable | Type | Range | What It Means |
| :--- | :--- | :--- | :--- | :--- |
| $\sigma_C$ | `stressLevel` | double | 0–3 | Overall caregiver stress (average) |
| $S_W$ | `workloadStress` | double | 0.5–3.0 | Stress from care hours **per week** |
| $S_F$ | `financialStress` | double | 0–3.0 | Stress from **weekly** income |
| $S_S$ | `sleepStress` | double | 0–3.0 | Stress from **weekly** sleep |
| $Q_C$ | `careQuality` | double | 0.2–1.0 | Quality of family care |
| $C_S$ | `copingSkills` | double | 0–1 | Caregiver's coping ability |
| $H_p$ | `workloadHoursPerWeek` | double | 0+ | Hours family spends on care **per week** |
| $I_w$ | `familyIncomeWeekly` | double | 0+ | **Weekly** family income |
| $S_h$ | `sleepQualityHourperWeek` | double | 0+ | Hours of sleep **per week** |
| $H(t)$ | `healthStatus` | double | 0–1 | Patient's health (1=healthy, 0=end-stage) |
| $B$ | `behaviouralSymptoms` | int | 0–10 | Behavioral symptom severity |
| $N_C$ | `careNeedsHoursPerWeek` | double | **28–140** | Hours of care needed **per week** |
| $Q_E$ | `effectiveCareQuality` | double | 0.1–1.0 | Combined family+prof. care quality |
| $P_h$ | `professionalCareHoursPerWeek` | double | 0+ | Hours of professional care **per week** |
| $r_d$ | `progressionRate` | double | **1.5** | Disease decline rate (as a percentage, e.g., 1.5 for 1.5%) |
| $w_x$ | (various) | double | 0-1 | Weights for stress components |

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
