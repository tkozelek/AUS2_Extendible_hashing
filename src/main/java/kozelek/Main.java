package kozelek;

import kozelek.US.HeapFile;
import kozelek.US.Block;
import kozelek.objekty.Car;
import kozelek.objekty.Servis;
import kozelek.objekty.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Main {
    public static void main(String[] args) {

        Random rand = new Random();

        int blokovacifaktor = 3;

        Servis sr = new Servis();
        Car cr = new Car();
        int servisSize = sr.getSize();
        int carSize = cr.getSize();

        System.out.printf("%d %d\n", servisSize * 5, carSize);


        HeapFile<Car> heap = new HeapFile<>("heapfile.bin", blokovacifaktor, Car.class);
        ArrayList<Car> cars = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Car car = new Car("meno", "priezvisko", 1, "SPZSOA");
            Servis servis = new Servis(LocalDate.now(), 565.0);
            servis.addPopis("oprava kolesa");
            servis.addPopis("oprava kolesa");
            servis.addPopis("oprava kolesa");
            car.addZaznam(servis);
            cars.add(car);

            heap.insert(car);
        }


        heap.printAll();
        System.out.println(heap);
        System.out.println(cars.size());
        System.out.println(heap.getRecordCount());

        heap.closeFileManager();
    }
}