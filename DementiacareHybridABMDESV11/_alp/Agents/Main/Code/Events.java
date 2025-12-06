void WeeklyStressUpdate()
{/*ALCODESTART::1763768351391*/
int countSevereDementia = 0;
// WEEKLY UPDATE - Runs every 7 days
traceln("=== Week " + (time() / 7) + " Update ===");

// CAREGIVERS: Update stress and care quality
for (Caregiver c : caregivers) {
    // 1. UPDATE INPUT VARIABLES from patient
    if (c.myPatient != null) {
        c.workloadHoursPerWeek = c.myPatient.careNeedHoursPerWeek;
        
        // Calculate sleep loss from patient's behavioral symptoms
        double baseSleepPerWeek = 56.0; // 8 hours * 7 days
        double sleepLossPerWeek = c.myPatient.behaviourSymptomSeverity * 2.8;
        c.sleepQualityHoursPerWeek = Math.max(0, baseSleepPerWeek - sleepLossPerWeek);
        
        if (c.myPatient.DementiaProgressionStatechart.isStateActive(PatientWithDem.SevereDementia)) {
        	countSevereDementia += 1;
        }
    }
    
    // 2. CONVERT TO STRESS SCORES (0-3 for each dimension)
    c.workloadCalculation();
    c.sleepQualityCalculation();
    c.weeklyIncomeCalculation();
    
    // 3. CALCULATE COMPOSITE STRESS (0-3 scale)
    c.calculateStressLevels();
    
    // 4. CALCULATE CARE QUALITY
    c.calculateCareQuality();
    
    // 5. APPLY WEEKLY DECAY
    c.applyCareQualityDecay();
    
    // 6. UPDATE COLOR based on stress state
    c.updateDistressColour();
}

traceln("Average stress: " + caregivers.stream().mapToDouble(c -> c.stressLevel).average().orElse(0));

dsSevereDementia.add(countSevereDementia);
/*ALCODEEND*/}

void ApplyInterventions()
{/*ALCODESTART::1763772773258*/
// Apply interventions if enabled
if (enableInterventions) {
    traceln("=== APPLYING INTERVENTIONS at week " + (time() / 7) + " ===");
    
    int highStressCount = 0;  // Track how many get services
    
    for (Caregiver c : caregivers) {
        // 1. Apply coping skills training to ALL caregivers
        c.applyCopingTraining();
        
        // 2. Service relief for elevated stress
        if (c.stressLevel > 1.5) {
            c.applyServiceRelief();
            c.inService = true;
            highStressCount++;
            traceln("  Caregiver enrolled in service relief (stress: " + 
                    String.format("%.2f", c.stressLevel) + ")");
            
            // 3. If patient exists, enroll in adult day care
            if (c.myPatient != null) {
                c.myPatient.inAdultDayCare = true;
                c.myPatient.professionalCareHours = 30.0; // 30 hrs/week professional care
                traceln("    Patient enrolled in adult day care");
            }
        }
    }
    
    traceln("Coping training applied to all " + caregivers.size() + " caregivers");
    traceln("Service relief provided to " + highStressCount + " high-stress caregivers");
}

/*ALCODEEND*/}

void recordWeeklyData()
{/*ALCODESTART::1764260341189*/
int week = (int) time();

for (Caregiver c : caregivers) {
	if (csvWriter != null) {
	   csvWriter.println(
	        c.getId() + "," +
	        week + "," +
	        c.stressLevel + "," +
	        c.sleepQualityHoursPerWeek + "," +
	        c.workloadHoursPerWeek + "," +
	        c.familyWeeklyIncome + "," +
	        c.careQuality
	    );
	}
}

/*ALCODEEND*/}

void recordWeeklyPatientData()
{/*ALCODESTART::1764879082115*/
int week = (int) time();

for (PatientWithDem p : patientWithDem) {
	if(patientCsvWriter != null){
	   patientCsvWriter.println(
	        p.getId() + "," +
	        week + "," +
	      	p.healthStatus + "," +
	   		p.careNeedHoursPerWeek + "," +
	    	p.diseaseProgressionRate + "," +
	    	p.behaviourSymptomSeverity
	    );
    }
}
/*ALCODEEND*/}

