package com.williamfzc.sibyl.core.analyzer;

import com.williamfzc.sibyl.core.model.clazz.Clazz;
import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.Log;
import com.williamfzc.sibyl.core.utils.SibylUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        Map<String, Set<Clazz>> clazzMap = createClazzMap();
        storage.getData().forEach(each -> analyzeSingle(each, snapshotMap, clazzMap));

        // in place modify
        return new Result<>();
    }

    private void analyzeSingle(
            Edge eachEdge, Map<String, Set<Method>> snapshotMap, Map<String, Set<Clazz>> clazzMap) {
        String targetType = eachEdge.getRawEdge().getCallerType();
        String targetMethodName = eachEdge.getRawEdge().getToMethodName();

        Method target = searchTargetMethod(targetType, targetMethodName, snapshotMap, clazzMap);
        eachEdge.setTarget(target);
    }

    private Method searchTargetMethod(
            String type,
            String name,
            Map<String, Set<Method>> snapshotMap,
            Map<String, Set<Clazz>> clazzMap) {
        if ("".equals(type)) {
            // give up ..
            return null;
        }

        if (!snapshotMap.containsKey(type)) {
            // this type can be substring
            for (String eachFullName : clazzMap.keySet()) {
                if (SibylUtils.getClazzNameFromPackageName(eachFullName).equals(type)) {
                    // replace with real name and search again
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
        for (Clazz eachSuperType : clazzMap.get(type)) {
            Method m =
                    searchTargetMethod(eachSuperType.getSuperName(), name, snapshotMap, clazzMap);
            if (null != m) {
                return m;
            }
        }
        return null;
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
        // k: class name with package
        // v: method set
        Map<String, Set<Method>> snapshotMap = new HashMap<>();
        snapshot.getData().stream()
                .filter(each -> each.getBelongsTo().getClazz() != null)
                .forEach(
                        each -> {
                            String k = each.getBelongsTo().getClazz().getFullName();
                            snapshotMap.putIfAbsent(k, new HashSet<>());
                            snapshotMap.get(k).add(each);
                        });
        return snapshotMap;
    }

    private Map<String, Set<Clazz>> createClazzMap() {
        // k: class name with package
        // v: clazz set (because of anonymous classes
        Map<String, Set<Clazz>> clazzMap = new HashMap<>();
        clazzGraph.getData().stream()
                .filter(each -> !each.getFullName().isEmpty())
                .forEach(
                        each -> {
                            String k = each.getFullName();
                            clazzMap.putIfAbsent(k, new HashSet<>());
                            clazzMap.get(k).add(each);
                        });
        return clazzMap;
    }
}
