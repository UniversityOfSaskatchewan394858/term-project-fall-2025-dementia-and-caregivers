double progressDisease()
{/*ALCODESTART::1761808652151*/
// Get caregiver quality (0.0 if no caregiver which is not possible in our model)
    double careQuality = (myCaregiver != null) ? myCaregiver.careQuality : 0.0;
    
    // Calculate weekly decline (diseaseProgressionRate is ALREADY percentage like 0.002 = 0.2%)
    // careQuality reduces decline (0.5 factor means perfect care cuts decline in half)
    double weeklyDecline = this.diseaseProgressionRate * (1.0 - careQuality * 0.3);
    
    // Stops progression at severe stage
    double minimumHealth = 0.05;  // Never go below 5% (severe but still tracking)
    this.healthStatus = Math.max(minimumHealth, this.healthStatus - weeklyDecline);
    
    // Update behavioral symptoms (0-10 scale, higher when health is lower)
    this.behaviourSymptomSeverity = Math.max(0, Math.min(10, (int)(10 * (1.0 - this.healthStatus))));
    
    // Calculate care needs based on health status and stage
    getCareNeedsHoursPerWeek();
    
    // Update dementia color based on current health
    updateDementiaColour();
    
    // Log progress every 10 weeks
    if ((int)(time() / 7) % 10 == 0) {
        traceln("Week " + (time() / 7) + " - Patient[" + getIndex() + 
                "] | Health: " + String.format("%.3f", healthStatus) + 
                " | Stage: " + getDementiaStage() + 
                " | Care Needs: " + String.format("%.1f", careNeedHoursPerWeek) + " hrs/week");
    }

/*ALCODEEND*/}

double updateDementiaColour()
{/*ALCODESTART::1763793331864*/
// Use health thresholds to determine color
        
    if (healthStatus >= 0.35) {
        demColour = yellow;      // Mild
    } else if (healthStatus >= 0.15) {
        demColour = orange;      // Moderate
    } else {
        demColour = red;         // Severe
    }
      
    // Apply color to visual indicator circle shape
    if (dementiaIndicator != null) {
        dementiaIndicator.setFillColor(demColour);
    }
/*ALCODEEND*/}

double getCareNeedsHoursPerWeek()
{/*ALCODESTART::1763832711645*/
// Set care needs based on STAGE with built-in caps
    double maxCareNeeds;
    if (inState(MildDementia)) {
        maxCareNeeds = 20.0;   // 20 hrs/week max
    } else if (inState(ModerateDementia)) {
        maxCareNeeds = 60.0;   // 60 hrs/week max
    } else if (inState(SevereDementia)) {
        maxCareNeeds = 100.0;  // 100 hrs/week max
    } else {
        maxCareNeeds = 120.0;  // End-stage max
    }

    double baseNeed = 4 * (1.0 - healthStatus) * 16;
    careNeedHoursPerWeek = Math.min(baseNeed, maxCareNeeds);
    careNeedHoursPerWeek = Math.max(10.0, careNeedHoursPerWeek);
    careNeedHoursPerWeek = Math.max(0, careNeedHoursPerWeek - adultDayCareHoursThisWeek);
    
    
    return careNeedHoursPerWeek;
/*ALCODEEND*/}

int getDementiaStage()
{/*ALCODESTART::1763844655589*/
if (inState(ModerateDementia)) {
        return 3;
    } else if (inState(SevereDementia)) {
        return 4;
    }
    return 2;  // Default to mild if somehow not in either state
/*ALCODEEND*/}

double exportCSV(PrintWriter pw,int week)
{/*ALCODESTART::1764549050374*/
pw.println(
    getId() + "," +
    week + "," +
    healthStatus + "," +
    careNeedHoursPerWeek + "," +
    diseaseProgressionRate + "," +
    behaviourSymptomSeverity
);
/*ALCODEEND*/}

