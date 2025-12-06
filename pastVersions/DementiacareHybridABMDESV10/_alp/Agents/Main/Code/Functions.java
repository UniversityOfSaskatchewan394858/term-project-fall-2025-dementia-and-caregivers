double positionAgentsInHomes()
{/*ALCODESTART::1763855561870*/
double offset = 8.0;

for (PatientWithDem patient : patientWithDem) {
    if (patient.myHome != null && patient.myCaregiver != null) {
        double homeX = patient.myHome.getX();
        double homeY = patient.myHome.getY();
        
        patient.setXY(homeX - offset, homeY);
        patient.myCaregiver.setXY(homeX + offset, homeY);
    }
}

traceln("All agents positioned!");

/*ALCODEEND*/}

double positionHomesOnGrid()
{/*ALCODESTART::1763856635573*/
int frameHeight = 1430;
int frameWidth = 1280;

int homesPerRow = (int) Math.ceil(Math.sqrt(countHomes));
double xSpacing = frameWidth / homesPerRow;
double ySpacing = frameHeight / homesPerRow;

for (int i = 0; i < homes.size(); i++) {
    int row = i / homesPerRow;
    int col = i % homesPerRow;
    
    double x = col * xSpacing;
    double y = row * ySpacing;
    
    homes.get(i).setXY(x, y);
}

traceln("Positioned " + homes.size() + " homes in grid");
/*ALCODEEND*/}

