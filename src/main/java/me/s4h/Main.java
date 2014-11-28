package me.s4h;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;


/**
 * Created by LENOVO on 2014/11/25.
 */
public class Main {

    private static GongjiaoService service;
    private static Scanner sc;

    public static void main(String[] args) throws FileNotFoundException {
        service = new GongjiaoService(new FileInputStream("busLinesInfo.txt"));
        sc = new Scanner(System.in);
        while (true) {
            System.out.println("select choice:");
            System.out.println("-1 exit");
            System.out.println("1 线路查询（查询某条线路经过的公交站点)");
            System.out.println("2 站点查询（查询经过某个站点的公交线路）");
            System.out.println("3 换乘查询");
            System.out.println("4 数据更新（可以重新加载数据）");
            switch (sc.nextInt()) {
                case -1:
                    return;
                case 1:
                    lineQuery();
                    break;
                case 2:
                    stopQuery();
                    break;
                case 3:
                    RouteQuery();
                    break;
                case 4:
                    System.out.println("reloading");
                    service = new GongjiaoService(new FileInputStream("busLinesInfo.txt"));
                    System.out.println("reload finish");
                    break;
            }
        }

    }

    private static void RouteQuery() {
        while (true) {
            System.out.println("input stopA name(-1 for return)");
            String stopAName = sc.next();
            if ("-1".equals(stopAName)) {
                return;
            } else {
                System.out.println("input stopB name");
                String stopBName = sc.next();
                List<GongjiaoService.Route> routes = service.findRoute(stopAName, stopBName);
                System.out.println("found " + routes.size() + " routes");
                for (int i = 0; i < routes.size(); i++) {
                    GongjiaoService.Route route = routes.get(i);
                    System.out.println("route " + (i + 1) + " (stops:" + route.totalDistance + "):");
                    for (GongjiaoService.Route.PartLine partLine : route.partLines) {
                        System.out.print(partLine.lineName + ":");
                        for (int j = 0; j < partLine.stops.size(); j++) {
                            System.out.print(partLine.stops.get(j).stopName + (j == partLine.stops.size() - 1 ? "" : " -> "));
                        }
                        System.out.println();
                    }
                    System.out.println();
                }
            }
        }
    }

    private static void stopQuery() {
        while (true) {
            System.out.println("input stopName( -1 for return ):");
            String stopName = sc.next();
            if ("-1".equals(stopName)) {
                return;
            } else {
                Set<String> lines = service.getLinesByStopName(stopName);
                System.out.println("lines for " + stopName);
                for (String line : lines) {
                    System.out.print(line + "\t");
                }
                System.out.println();
            }
        }
    }


    public static void lineQuery() {
        while (true) {
            System.out.println("input lineName( -1 for return ):");
            String lineName = sc.next();
            if ("-1".equals(lineName)) {
                return;
            } else {
                Map<Integer, Map<Integer, GongjiaoService.Route.PartLine.Stop>> diLines = service.getLine(lineName);
                if (diLines == null) {
                    System.out.println("no match for " + lineName);
                } else {
                    System.out.println("line for " + lineName);
                    for (Map<Integer, GongjiaoService.Route.PartLine.Stop> diLine : diLines.values()) {
                        for (Integer index : diLine.keySet()) {
                            System.out.print(diLine.get(index).stopName + (index == diLine.size() ? "" : " -> "));
                        }

                        System.out.println();
                    }
                }
            }
        }
    }
}


