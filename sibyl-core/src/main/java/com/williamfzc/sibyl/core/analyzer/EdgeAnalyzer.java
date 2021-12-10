package com.williamfzc.sibyl.core.analyzer;

import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.Log;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EdgeAnalyzer extends BaseAnalyzer<Edge> {
    private Storage<Method> snapshot = null;
    private Storage<Clazz> clazzGraph = null;

    public void setSnapshot(Storage<Method> snapshot) {
        this.snapshot = snapshot;
    }

    public void setClazzGraph(Storage<Clazz> clazzGraph) {
        this.clazzGraph = clazzGraph;
    }

    @Override
    public Result<Edge> analyze(Storage<Edge> storage) {
        if (!verify()) {
            return null;
        }

        // TODO: class name can be same !
        Map<String, Set<Method>> snapshotMap = createSnapshotMap();
        Map<String, Clazz> clazzMap = createClazzMap();
        storage.getData().forEach(each -> analyzeSingle(each, snapshotMap, clazzMap));

        // in place modify
        return new Result<>();
    }

    private void analyzeSingle(
            Edge eachEdge, Map<String, Set<Method>> snapshotMap, Map<String, Clazz> clazzMap) {
        String targetType = eachEdge.getRawEdge().getCallerType();
        String targetMethodName = eachEdge.getRawEdge().getToMethodName();

        Method target = searchTargetMethod(targetType, targetMethodName, snapshotMap, clazzMap);
        eachEdge.setTarget(target);
    }

    private Method searchTargetMethod(
            String type,
            String name,
            Map<String, Set<Method>> snapshotMap,
            Map<String, Clazz> clazzMap) {
        if (!snapshotMap.containsKey(type)) {
            // this type can be substring
            for (String eachFullName : clazzMap.keySet()) {
                if (eachFullName.endsWith(type)) {
                    return searchTargetMethod(eachFullName, name, snapshotMap, clazzMap);
                }
            }
            // not found, give up
            return null;
        }

        // search method in this class
        for (Method eachMethod : snapshotMap.get(type)) {
            if (eachMethod.getInfo().getName().equals(name)) {
                // match
                Log.info(String.format("found method %s in class %s", name, type));
                return eachMethod;
            }
        }

        // still did not find this method
        if (!clazzMap.containsKey(type)) {
            // give up
            return null;
        }
        return searchTargetMethod(clazzMap.get(type).getSuperName(), name, snapshotMap, clazzMap);
    }

    private boolean verify() {
        if (null == snapshot) {
            Log.error("setSnapshot first");
            return false;
        }
        if (null == clazzGraph) {
            Log.error("setClazzGraph first");
            return false;
        }
        return true;
    }

    private Map<String, Set<Method>> createSnapshotMap() {
        // k: class name
        // v: method set
        Map<String, Set<Method>> snapshotMap = new HashMap<>();
        snapshot.getData()
                .forEach(
                        each -> {
                            String k = each.getBelongsTo().getClazz().getFullName();
                            if (!snapshotMap.containsKey(k)) {
                                snapshotMap.put(k, new HashSet<>());
                            }
                            snapshotMap.get(k).add(each);
                        });
        return snapshotMap;
    }

    private Map<String, Clazz> createClazzMap() {
        return clazzGraph.getData().stream()
                .collect(Collectors.toMap(Clazz::getFullName, each -> each));
    }
}
