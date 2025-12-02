double positionAgentsInHomes()
{/*ALCODESTART::1763855561870*/
double offset = 8.0;

for (PatientWithDem patient : patientWithDem) {
    if (patient.home != null && patient.myCaregiver != null) {
        double homeX = patient.home.getX();
        double homeY = patient.home.getY();
        
        patient.setXY(homeX - offset, homeY);
        patient.myCaregiver.setXY(homeX + offset, homeY);
    }
}

traceln("All agents positioned!");

/*ALCODEEND*/}

double positionHomesOnGrid()
{/*ALCODESTART::1763856635573*/
int homesPerRow = 5;
double spacing = 60;

for (int i = 0; i < homes.size(); i++) {
    int row = i / homesPerRow;
    int col = i % homesPerRow;
    
    double x = col * spacing;
    double y = row * spacing;
    
    homes.get(i).setXY(x, y);
}

traceln("Positioned " + homes.size() + " homes in grid");

/*ALCODEEND*/}

