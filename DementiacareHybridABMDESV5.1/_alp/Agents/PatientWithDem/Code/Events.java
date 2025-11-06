void WeeklyDiseaseProgression()
{/*ALCODESTART::1761809107999*/
// Calculate weekly decline
double decline = this.diseaseProgressionRate * (1.0 - this.careQualityEffectiveness * 0.5);
this.healthStatus = Math.max(0, this.healthStatus - decline / 100.0);

// Update behavioral symptoms (0-10 scale)
this.behaviourSymptomSeverity = (int)(10 * (1.0 - this.healthStatus));

// Calculate care needs (4-20 hours per day)
double baseCareNeeds = 4 + (1.0 - this.healthStatus) * 16;
double careReduction = this.professionalCareHours * 0.02;
this.careNeedHours = Math.max(4, baseCareNeeds - careReduction);

// LINK TO CAREGIVER: Update caregiver workload
if (this.myCaregiver != null) {
    // Send care needs back to caregiver
    // (For now, just log - we'll connect in Main later)
    traceln(time() + " - " + this.getName() + 
            " | Health: " + String.format("%.3f", this.healthStatus) + 
            " | Stage: " + this.dementiaStage + 
            " | Symptoms: " + this.behaviourSymptomSeverity + 
            " | Care Needs: " + String.format("%.1f", this.careNeedHours) + " hrs/day");
}
/*ALCODEEND*/}

