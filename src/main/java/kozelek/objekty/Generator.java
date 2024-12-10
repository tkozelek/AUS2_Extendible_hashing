package kozelek.objekty;

import kozelek.Helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;

public class Generator {
    private final Random rand;

    private static final String[] servisDesc = {
            "vymena olej", "pneumatiky", "brzdy", "motor", "bateria",
            "geometria", "filtre"
    };

    public static String[] names = {"Peter","Palo","Michal","Tomas","Fero","Brano"};
    public static String[] lastnames = {"Ackers","Addiss","Ainscoe","Aish","Akess","Alefounder"};

    public Generator() {
        this.rand = new Random();
    }

    public ArrayList<Car> generujIncrement(int od, int pocet, int servisCount, int popisCount) {
        ArrayList<Car> cars = new ArrayList<>();
        for (int i = od; i < pocet + od; i++) {
            StringBuilder spz = new StringBuilder();
            for (int j = 0; j < 4; j++) { // 4 znaky spz
                spz.append((char) (rand.nextInt(26) + 'a'));
            }
            String name = names[rand.nextInt(names.length)];
            String lastname = lastnames[rand.nextInt(lastnames.length)];

            Car car = new Car(name, lastname, i, spz.toString());

            ArrayList<Servis> servisy = this.generujServisy(servisCount, popisCount);
            for (Servis s : servisy) {
                car.addServis(s);
            }
            cars.add(car);
        }
        return cars;
    }

    public ArrayList<Car> generuj(int pocet) {
        ArrayList<Car> cars = new ArrayList<>();
        for (int i = 0; i < pocet; i++) {
            int id = rand.nextInt(Integer.MAX_VALUE-1);
            StringBuilder spz = new StringBuilder();
            for (int j = 0; j < 4; j++) { // 4 znaky spz
                spz.append((char) (rand.nextInt(26) + 'a'));
            }
            String name = names[rand.nextInt(names.length)];
            String lastname = lastnames[rand.nextInt(lastnames.length)];
            Car car = new Car(name, lastname, id, spz.toString());
            ArrayList<Servis> servisy = this.generujServisy();
            for (Servis s : servisy) {
                car.addServis(s);
            }
            cars.add(car);
        }
        return cars;
    }

    private ArrayList<Servis> generujServisy() {
        ArrayList<Servis> serviceRecords = new ArrayList<>();
        int recordCount = rand.nextInt(Helper.MAX_POCET_SERVISOV + 1);

        for (int i = 0; i < recordCount; i++) {
            LocalDate date = generateRandomDate();
            double cost = rand.nextDouble(1000);

            Servis servis = new Servis(date, cost);

            int descriptionCount = rand.nextInt(Helper.MAX_POPIS_POCET + 1);
            for (int j = 0; j < descriptionCount; j++) {
                String description = servisDesc[rand.nextInt(servisDesc.length)];
                servis.addPopis(description);
            }

            serviceRecords.add(servis);
        }

        return serviceRecords;
    }

    private ArrayList<Servis> generujServisy(int servisCount, int popisCount) {
        int maxPocet = Math.min(servisCount, Helper.MAX_POCET_SERVISOV);
        ArrayList<Servis> serviceRecords = new ArrayList<>();
        int recordCount = rand.nextInt(maxPocet + 1);

        for (int i = 0; i < recordCount; i++) {
            LocalDate date = generateRandomDate();
            double cost = rand.nextDouble(1000);

            Servis servis = new Servis(date, cost);

            int descCount = Math.min(popisCount, Helper.MAX_POPIS_POCET);
            int descriptionCount = rand.nextInt(descCount + 1);
            for (int j = 0; j < descriptionCount; j++) {
                String description = servisDesc[rand.nextInt(servisDesc.length)];
                servis.addPopis(description);
            }

            serviceRecords.add(servis);
        }

        return serviceRecords;
    }

    private LocalDate generateRandomDate() {
        int year = rand.nextInt(5) + 2019; // den
        int month = rand.nextInt(12) + 1; // m
        int day = rand.nextInt(28) + 1; // rok
        return LocalDate.of(year, month, day);
    }
}
