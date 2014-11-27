package me.s4h;

import java.io.InputStream;
import java.util.*;

/**
 * Created by LENOVO on 2014/11/25.
 */
class GongjiaoService {
    private List<Record> records = new ArrayList<Record>();
    private List<Through> throughs = new ArrayList<Through>();

    private Map<Integer, Set<String>> lineNamesByStopId = new HashMap<Integer, Set<String>>();

    private Map<String, Integer> stopIdByStopName = new HashMap<String, Integer>();


    private Map<String, Map<Integer, Map<Integer, Route.PartLine.Stop>>> lines =
            new HashMap<String, Map<Integer, Map<Integer, Route.PartLine.Stop>>>();


    public Integer getStopIdByStopName(String stopName) {
        return stopIdByStopName.get(stopName);
    }

    public Map<Integer, Map<Integer, Route.PartLine.Stop>> getLine(String lineName) {
        return lines.get(lineName);
    }


    public Set<String> getLinesByStopId(int stopId) {
        return lineNamesByStopId.get(stopId);
    }


    private static List<Route> sortedRoute(List<Route> routes) {
        routes.sort(new Comparator<Route>() {
            @Override
            public int compare(Route o1, Route o2) {
                return o1.totalDistance - o2.totalDistance;
            }
        });
        return routes.subList(0, 8 > routes.size() ? routes.size() : 8);
    }

    public List<Route> findRoute(int stopAId, int stopBId) {
        List<Route> routes = new ArrayList<Route>();

        List<Through> throughFromA = new ArrayList<Through>();
        List<Through> throughToB = new ArrayList<Through>();
        List<Through> throughOther = new ArrayList<Through>();
        for (Through t : throughs) {
            if (t.stopAId == stopAId) { //from A
                if (t.stopBId == stopBId) { // and to B
                    Route route = new Route();
                    route.totalDistance = t.distance;
                    Route.PartLine partLine = new Route.PartLine(t.lineId, t.lineName, t.lineDirection, t.distance);
                    for (Record r : records) {
                        if (r.lineId == t.lineId && t.lineDirection == r.lineDirection
                                && r.lineStopIndex >= t.stopAIndex && r.lineStopIndex <= t.stopBIndex) {
                            partLine.stops.put(r.lineStopIndex, new Route.PartLine.Stop(r.stopId, r.stopName));
                        }
                    }
                    route.partLines.add(partLine);
                    routes.add(route);
                } else { //but not to B
                    throughFromA.add(t);
                }
            } else { //not from A
                if (t.stopBId == stopBId) { //but to B
                    throughToB.add(t);
                } else { //
                    throughOther.add(t);
                }
            }
        }
        if (routes.size() > 0) {
            return sortedRoute(routes);
        }

        boolean secondPlanFound = false;
        List<Route> routesThirdPlann = new ArrayList<Route>();
        for (Through t1 : throughFromA) {
            for (Through t2 : throughToB) {
                if (t1.stopBId == t2.stopAId) {
                    secondPlanFound = true;

                    Route route = new Route();
                    route.totalDistance = t1.distance + t2.distance;
                    Route.PartLine partLine1 = new Route.PartLine(t1.lineId, t1.lineName, t1.lineDirection, t1.distance);
                    Route.PartLine partLine2 = new Route.PartLine(t2.lineId, t2.lineName, t2.lineDirection, t2.distance);

                    for (Record r : records) {
                        if (r.lineId == t1.lineId && r.lineDirection == t1.lineDirection
                                && r.lineStopIndex >= t1.stopAIndex && r.lineStopIndex <= t1.stopBIndex) {
                            partLine1.stops.put(r.lineStopIndex, new Route.PartLine.Stop(r.stopId, r.stopName));

                        } else if (r.lineId == t2.lineId && r.lineDirection == t2.lineDirection
                                && r.lineStopIndex >= t2.stopAIndex && r.lineStopIndex <= t2.stopBIndex) {
                            partLine2.stops.put(r.lineStopIndex, new Route.PartLine.Stop(r.stopId, r.stopName));
                        }
                    }

                    route.partLines.add(partLine1);
                    route.partLines.add(partLine2);
                    routes.add(route);

                } else if (!secondPlanFound) {
                    for (Through t3 : throughOther) {
                        if (t3.stopAId == t1.stopBId && t3.stopBId == t2.stopAId) {
                            Route route = new Route();
                            route.totalDistance = t1.distance + t2.distance + t3.distance;
                            Route.PartLine partLine1 = new Route.PartLine(t1.lineId, t1.lineName, t1.lineDirection, t1.distance);
                            Route.PartLine partLine2 = new Route.PartLine(t2.lineId, t2.lineName, t2.lineDirection, t2.distance);
                            Route.PartLine partLine3 = new Route.PartLine(t3.lineId, t3.lineName, t3.lineDirection, t3.distance);
                            for (Record r : records) {
                                if (r.lineId == t1.lineId && r.lineDirection == t1.lineDirection
                                        && r.lineStopIndex >= t1.stopAIndex && r.lineStopIndex <= t1.stopBIndex) {
                                    partLine1.stops.put(r.lineStopIndex, new Route.PartLine.Stop(r.stopId, r.stopName));

                                } else if (r.lineId == t2.lineId && r.lineDirection == t2.lineDirection
                                        && r.lineStopIndex >= t2.stopAIndex && r.lineStopIndex <= t2.stopBIndex) {
                                    partLine2.stops.put(r.lineStopIndex, new Route.PartLine.Stop(r.stopId, r.stopName));
                                } else if (r.lineId == t3.lineId && r.lineDirection == t3.lineDirection
                                        && r.lineStopIndex >= t3.stopAIndex && r.lineStopIndex <= t3.stopBIndex) {
                                    partLine3.stops.put(r.lineStopIndex, new Route.PartLine.Stop(r.stopId, r.stopName));
                                }
                            }

                            route.partLines.add(partLine1);
                            route.partLines.add(partLine3);
                            route.partLines.add(partLine2);//the order matters
                            routesThirdPlann.add(route);
                        }
                    }
                }
            }
        }
        if (secondPlanFound) {
            return sortedRoute(routes);
        }

        return sortedRoute(routesThirdPlann);
    }


    public GongjiaoService(InputStream in) {
        Scanner sc = new Scanner(in, "gb2312");
        sc.nextLine();
        while (sc.hasNext()) {
            records.add(new Record(sc.nextInt(), sc.next(),
                    sc.nextInt(), sc.next(), sc.nextInt(), sc.nextInt()));
        }
        sc.close();
        System.out.println("read finish");


        for (int i = 0; i < records.size(); i++) {
            for (int j = i + 1; j < records.size(); j++) {
                Record r1 = records.get(i);
                Record r2 = records.get(j);
                if (r1.lineId == r2.lineId
                        && r1.lineDirection == r2.lineDirection
                        && r1.lineStopIndex < r2.lineStopIndex) {
                    Through t = new Through(r1.stopId, r2.stopId, r1.stopName,
                            r2.stopName, r1.lineStopIndex, r2.lineStopIndex,
                            r2.lineStopIndex - r1.lineStopIndex, r1.lineId, r1.lineName, r1.lineDirection);
                    throughs.add(t);
                }
            }
        }


        for (Record r : records) {
            if (lines.containsKey(r.lineName)) {
                Map<Integer, Map<Integer, Route.PartLine.Stop>> diLines = lines.get(r.lineName);
                if (diLines.containsKey(r.lineDirection)) {
                    Map<Integer, Route.PartLine.Stop> stopMap = diLines.get(r.lineDirection);
                    stopMap.put(r.lineStopIndex, new Route.PartLine.Stop(r.stopId, r.stopName));
                } else {
                    Map<Integer, Route.PartLine.Stop> stopMap = new HashMap<Integer, Route.PartLine.Stop>();
                    stopMap.put(r.lineStopIndex, new Route.PartLine.Stop(r.stopId, r.stopName));
                    diLines.put(r.lineDirection, stopMap);
                }
            } else {
                Map<Integer, Map<Integer, Route.PartLine.Stop>> diLines = new HashMap<Integer, Map<Integer, Route.PartLine.Stop>>();
                Map<Integer, Route.PartLine.Stop> stopMap = new HashMap<Integer, Route.PartLine.Stop>();
                stopMap.put(r.lineStopIndex, new Route.PartLine.Stop(r.stopId, r.stopName));
                diLines.put(r.lineDirection, stopMap);
                lines.put(r.lineName, diLines);
            }
        }


        for (Record r : records) {
            if (lineNamesByStopId.containsKey(r.stopId)) {
                lineNamesByStopId.get(r.stopId).add(r.lineName);
            } else {
                HashSet<String> lines = new HashSet<String>();
                lines.add(r.lineName);
                lineNamesByStopId.put(r.stopId, lines);
            }
        }


        for (Record r : records) {
            stopIdByStopName.put(r.stopName, r.stopId);
        }


        System.out.println("generate finish");
    }

    static class Route {
        public int totalDistance;

        public List<PartLine> partLines = new ArrayList<PartLine>();


        static class PartLine {
            public int lineId;
            public String lineName;
            public int lineDirection;
            public int distance;
            public Map<Integer, Stop> stops = new HashMap<Integer, Stop>();

            public PartLine(int lineId, String lineName, int lineDirection, int distance) {
                this.lineId = lineId;
                this.lineName = lineName;
                this.lineDirection = lineDirection;
                this.distance = distance;
            }

            static class Stop {
                public int stopId;
                public String stopName;
                public int stopIndex;
                public Stop(int stopId, String stopName) {
                    this.stopId = stopId;
                    this.stopName = stopName;
                }
            }
        }
    }


    static class Through {
        int stopAId;
        int stopBId;

        String stopAName;
        String stopBName;
        int stopAIndex;
        int stopBIndex;
        int distance;//stopBIndex-stopAIndex

        int lineId;
        String lineName;
        int lineDirection;

        public Through(int stopAId, int stopBId, String stopAName, String stopBName,
                       int stopAIndex, int stopBIndex, int distance,
                       int lineId, String lineName, int lineDirection) {
            this.stopAId = stopAId;
            this.stopBId = stopBId;
            this.stopAName = stopAName;
            this.stopBName = stopBName;
            this.stopAIndex = stopAIndex;
            this.stopBIndex = stopBIndex;
            this.distance = distance;
            this.lineId = lineId;
            this.lineName = lineName;
            this.lineDirection = lineDirection;
        }
    }

    static class Record {
        int lineId;
        String lineName;
        int stopId;
        String stopName;
        int lineStopIndex;
        int lineDirection;

        public Record(int lineId, String lineName, int stopId,
                      String stopName, int lineStopIndex, int lineDirection) {
            this.lineId = lineId;
            this.lineName = lineName;
            this.stopId = stopId;
            this.stopName = stopName;
            this.lineStopIndex = lineStopIndex;
            this.lineDirection = lineDirection;
        }

        @Override
        public String toString() {
            return "Record{" +
                    ", lineId=" + lineId +
                    ", lineName='" + lineName + '\'' +
                    ", stopId=" + stopId +
                    ", stopName='" + stopName + '\'' +
                    ", lineStopIndex=" + lineStopIndex +
                    ", lineDirection=" + lineDirection +
                    '}';
        }

        public Record() {
        }

    }
}
