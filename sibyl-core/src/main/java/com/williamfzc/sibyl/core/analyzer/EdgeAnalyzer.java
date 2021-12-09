package com.williamfzc.sibyl.core.analyzer;

import com.williamfzc.sibyl.core.model.edge.Edge;
import com.williamfzc.sibyl.core.model.method.Method;
import com.williamfzc.sibyl.core.storage.Storage;
import com.williamfzc.sibyl.core.utils.Log;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EdgeAnalyzer extends BaseAnalyzer<Edge> {
    private Storage<Method> snapshot = null;

    public void setSnapshot(Storage<Method> snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public Result<Edge> analyze(Storage<Edge> storage) {
        if (null == snapshot) {
            Log.error("setSnapshot first");
            return null;
        }

        // k: class name
        // v: method set
        Map<String, Set<Method>> snapshotMap = new HashMap<>();
        snapshot.getData()
                .forEach(
                        each -> {
                            String k = each.getBelongsTo().getClassName();
                            if (!snapshotMap.containsKey(k)) {
                                snapshotMap.put(k, new HashSet<>());
                            }
                            snapshotMap.get(k).add(each);
                            Log.info("found class: " + k);
                        });

        storage.getData()
                .forEach(
                        eachEdge -> {
                            String eachCallerType = eachEdge.getRawEdge().getCallerType();
                            Log.info("check type: " + eachCallerType);
                            if (snapshotMap.containsKey(eachCallerType)) {
                                // try to find this method
                                snapshotMap
                                        .get(eachCallerType)
                                        .forEach(
                                                eachMethod -> {
                                                    if (eachMethod
                                                            .getInfo()
                                                            .getName()
                                                            .equals(
                                                                    eachEdge.getRawEdge()
                                                                            .getToMethodName())) {
                                                        // match!
                                                        eachEdge.setTarget(eachMethod);
                                                    }
                                                });
                            }
                        });

        // in place modify
        return new Result<>();
    }
}
