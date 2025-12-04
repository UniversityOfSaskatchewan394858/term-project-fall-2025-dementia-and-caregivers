double calculateStressLevels()
{/*ALCODESTART::1761808353716*/
stressLevel = (w_W * workloadStress + w_F * financialStress + w_S * sleepStress) / (w_W + w_F + w_S);
/*ALCODEEND*/}

double calculateCareQuality()
{/*ALCODESTART::1761808475006*/
// Normalize quadratic stress to 0-1 scale
    double normalizedStress = stressLevel / 3.0;  // Max is 3.0
    
    // Calculate care quality
    double stressImpact = normalizedStress * 0.5;
    double copingBonus = copingSkillsBoostAmount * 0.2;
    
    careQuality = 0.8 - stressImpact + copingBonus;
    
    if (inService) {
    careQuality += 0.1;
	}
    
    // Clamp to valid range
    careQuality = Math.max(0.2, Math.min(1.0, careQuality));
/*ALCODEEND*/}

boolean applyServiceRelief()
{/*ALCODESTART::1761808523679*/
// Reduce stress by % i.e. 10% of stresslvl(one-time when entering support state)
    stressLevel = stressLevel * 0.9;
    inService = true;  // Flag that service is active
    traceln("Service relief applied. New stress: " + String.format("%.2f", stressLevel));

/*ALCODEEND*/}

double workloadCalculation()
{/*ALCODESTART::1763748845908*/
  // INPUT: workloadHoursPerWeek (could be 0-140)
    // OUTPUT: workloadStress (will be 0.0, 0.5, 1.5, or 3.0)
    
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
  
/*ALCODEEND*/}

double sleepQualityCalculation()
{/*ALCODESTART::1763748873018*/
// INPUT: sleepQualityHoursperWeek (could be 0-56)
    // OUTPUT: sleepStress (will be 0.0, 0.5, 1.5, or 3.0)
    
    if (sleepQualityHoursPerWeek >= 49) {
    sleepStress = 0.0;
} else if (sleepQualityHoursPerWeek >= 42) {
    sleepStress = 0.5;
} else if (sleepQualityHoursPerWeek >= 35) {
    sleepStress = 1.0;
} else if (sleepQualityHoursPerWeek >= 28) {
    sleepStress = 1.5;
} else if (sleepQualityHoursPerWeek >= 21) {
    sleepStress = 2.0;
} else if (sleepQualityHoursPerWeek >= 14) {
    sleepStress = 2.5;
} else {
    sleepStress = 3.0;
}

  
/*ALCODEEND*/}

double weeklyIncomeCalculation()
{/*ALCODESTART::1763748887573*/

    // INPUT: familyWeeklyIncome (could be $0–$3000+)
    // OUTPUT: financialStress (will be 0.0–2.0)
    
    double baseCost = 1500.0;  // Realistic weekly expenses for dementia care household
    
    double incomeThisWeek = familyWeeklyIncome;
    
    // Add stipend if enabled
    if (main.enableStipend) {
        incomeThisWeek += main.stipendAmount;  // e.g., +$200/week
    }
    
    // Calculate income ratio (buffering effect)
    double incomeRatio = incomeThisWeek / baseCost; 
    
    // Map ratio to financial stress (0–2 scale, bounded)
    // High income ratio -> low stress
    // Low income ratio -> high stress
    if (incomeRatio >= 1.5) {
        financialStress = 0.0;      // Income well above expenses (~$2250+/week)
    } else if (incomeRatio >= 1.2) {
        financialStress = 0.3;      // Comfortable buffer (~$1800/week)
    } else if (incomeRatio >= 1.0) {
        financialStress = 0.6;      // Breaking even (~$1500/week)
    } else if (incomeRatio >= 0.8) {
        financialStress = 1.0;      // Moderate strain (~$1200/week)
    } else if (incomeRatio >= 0.6) {
        financialStress = 1.3;      // High strain (~$900/week)
    } else if (incomeRatio >= 0.4) {
        financialStress = 1.7;      // Severe strain (~$600/week)
    } else {
        financialStress = 2.0;      // Critical (< $600/week, clamped at 2.0)
    }

/*ALCODEEND*/}

double applyCareQualityDecay()
{/*ALCODESTART::1763769335311*/
// Care quality decays if stress is high
    if (stressLevel > 1.5) {
        careQuality -= careQualityDecayRate;
        
        // Ensure it doesn't go below minimum
        careQuality = Math.max(0.2, careQuality);
    }
/*ALCODEEND*/}

double applyCopingTraining()
{/*ALCODESTART::1763769665895*/
 // Boost coping skills by the parameter amount
copingSkills += copingSkillsBoostAmount;
    
// Make sure it doesn't exceed maximum (1.0 = 100%)
copingSkills = Math.min(1.0, copingSkills);

/*ALCODEEND*/}

double updateDistressColour()
{/*ALCODESTART::1763772235496*/
 // Update distress color based on stress level thresholds
    if (stressLevel < 1.0) {
        DistressColour = green;        // LowStress
    } else if (stressLevel < 1.5) {
        DistressColour = yellow;       // ModerateStress
    } else if (stressLevel < 2.0) {
        DistressColour = orange;       // HighStress
    } else {
        DistressColour = red;          // CrisisState
    }
    
    // Override color if patient is in adult day care
    if (myPatient != null && myPatient.inAdultDayCare) {
        DistressColour = cyan;
    }
    
    // Apply color to visual indicator
    if (stressIndicator != null) {
        stressIndicator.setFillColor(DistressColour);
    }
/*ALCODEEND*/}

double exportCSV(PrintWriter pw,int week)
{/*ALCODESTART::1764258975690*/
pw.println(
    getId() + "," +
    week + "," +
    stressLevel + "," +
    sleepQualityHoursPerWeek + "," +
    workloadHoursPerWeek + "," +
    familyWeeklyIncome + "," +
    careQualityEffectiveness
);
/*ALCODEEND*/}

