package kozelek;

import kozelek.US.HeapFile;
import kozelek.US.Block;
import kozelek.objekty.Car;
import kozelek.objekty.Servis;
import kozelek.objekty.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Main {
    public static void main(String[] args) {

        Random rand = new Random();

        int blokovacifaktor = 3;

        HeapFile<Car> heap = new HeapFile<>("heapfile.bin", blokovacifaktor, Car.class);
        ArrayList<Car> cars = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Car car = new Car("Tomas" + i, "Koze" + i, i, "SPZ" + i);
            Servis servis = new Servis(new Date(rand.nextLong()), rand.nextInt(5000));
            servis.addPopis("pp1 " + i);
            servis.addPopis("pp1 " + i);
            servis.addPopis("pp1 " + i);
            Servis servis2 = new Servis(new Date(rand.nextLong()), rand.nextInt(5000));
            servis2.addPopis("pp2 " + i);
            Servis servis3 = new Servis(new Date(rand.nextLong()), rand.nextInt(5000));
            servis3.addPopis("pp3 " + i);
            car.addZaznam(servis);
            car.addZaznam(servis2);
            car.addZaznam(servis3);
            cars.add(car);
            int index = heap.insert(car);
            indexes.add(index);
        }

        long sizeBefore = heap.fileSize();


        long sizeAfter = heap.fileSize();

        heap.printAll();
        System.out.println(heap);
        System.out.println(cars.size());
        System.out.println(heap.getRecordCount());
        System.out.format("%d %d", sizeBefore, sizeAfter);

        heap.closeFileManager();
    }
}