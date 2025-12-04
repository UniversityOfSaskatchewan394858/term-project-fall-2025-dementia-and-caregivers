double calculateStressLevels()
{/*ALCODESTART::1761808353716*/
stressLevel = Math.pow(w_W * workloadStress,2)+Math.pow(w_F * financialStress,2)+Math.pow(w_S * sleepStress,2);

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
// Check if caregiver qualifies for relief services
    // For example: if their stress level is moderate or high
  //  if (stressLevel > 1.5) {
    //    return true;  // Yes, they qualify
 //   }
  //  return false;  
  // No, they don't qualify
 double reliefAmount = 0.15;  // Reduce stress by 15%
    this.stressLevel = Math.max(0, this.stressLevel - reliefAmount);
    traceln("Service relief applied! New stress: " + this.stressLevel);
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
// INPUT: familyWeeklyIncome (could be $0-$3000+)
    // OUTPUT: financialStress (will be 0.0, 0.5, 1.5, or 3.0)
    
    if (familyWeeklyIncome >= 1900) {
    financialStress = 0.0;
} else if (familyWeeklyIncome >= 1300) {
    financialStress = 0.5;
} else if (familyWeeklyIncome >= 900) {
    financialStress = 1.0;
} else if (familyWeeklyIncome >= 700) {
    financialStress = 1.5;
} else if (familyWeeklyIncome >= 500) {
    financialStress = 2.0;
} else {
    financialStress = 3.0;
}

  
/*ALCODEEND*/}

double applyCareQualityDecay()
{/*ALCODESTART::1763769335311*/
// Care quality decays if stress is high
    if (stressLevel > 2.0) {
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
    } else if (stressLevel < 1.8) {
        DistressColour = yellow;       // ModerateStress
    } else if (stressLevel < 2.5) {
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

