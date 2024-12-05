package kozelek;

import kozelek.US.hash.ExtendibleHashing;
import kozelek.US.heap.HeapFile;
import kozelek.objekty.Car;
import kozelek.objekty.HashObjekty.CarId;
import kozelek.objekty.HashObjekty.CarSPZ;
import kozelek.objekty.Servis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        ExtendibleHashing<CarId> ext = new ExtendibleHashing<>("carid.bin", CarId.class, 3);
        ExtendibleHashing<CarSPZ> extSPZ = new ExtendibleHashing<>("carspz.bin", CarSPZ.class, 3);

        for (int i = 1; i < 11; i++) {
            CarId carId = new CarId(i, i);
            ext.insert(carId);
        }

        System.out.println();
        ext.printAll();

        System.out.println();


//        Random rand = new Random();
//
//        int blokovacifaktor = 3;
//
//
//        HeapFile<Car> heap = new HeapFile<>("heapfilee.bin", 5, Car.class);
//        ArrayList<Car> cars = new ArrayList<>();
//        ArrayList<Integer> indexes = new ArrayList<>();
//
//        for (int i = 0; i < 134; i++) {
//            Car car = new Car("meno" + i, "priezvisko" + i, i, "SP" + i);
//            Servis servis = new Servis(LocalDate.now(), 565.0);
//            servis.addPopis("oprava kolesa");
//            servis.addPopis("oprava kolesa");
//            servis.addPopis("oprava kolesa");
//            car.addZaznam(servis);
//            cars.add(car);
//
//            int a = heap.insert(car);
//            indexes.add(a);
//        }
//
//        heap.printAll();
//        System.out.println(heap);
//        System.out.println(cars.size());
//        System.out.println(heap.getRecordCount());
//
//        heap.saveState();
//        heap.closeFileManager();
    }
}