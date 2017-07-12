package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ScenarioProcessor {

    public void putRepeatedRequestsInScenarios(List<StubMapping> stubMappings) {
        ImmutableListMultimap<RequestPattern, StubMapping> stubsGroupedByRequest = Multimaps.index(stubMappings, new Function<StubMapping, RequestPattern>() {
            @Override
            public RequestPattern apply(StubMapping mapping) {
                return mapping.getRequest();
            }
        });

        Map<RequestPattern, Collection<StubMapping>> groupsWithMoreThanOneStub = Maps.filterEntries(stubsGroupedByRequest.asMap(), new Predicate<Map.Entry<RequestPattern, Collection<StubMapping>>>() {
            @Override
            public boolean apply(Map.Entry<RequestPattern, Collection<StubMapping>> input) {
                return input.getValue().size() > 1;
            }
        });

        for (Map.Entry<RequestPattern, Collection<StubMapping>> entry: groupsWithMoreThanOneStub.entrySet()) {
            putStubsInScenario(ImmutableList.copyOf(entry.getValue()));
        }
    }

    private void putStubsInScenario(List<StubMapping> stubMappings) {
        StubMapping firstScenario = stubMappings.get(0);
        String scenarioName = "scenario-" + Urls.urlToPathParts(URI.create(firstScenario.getRequest().getUrl()));

        int count = 1;
        for (StubMapping stub: stubMappings) {
            stub.setScenarioName(scenarioName);
            if (count == 1) {
                stub.setRequiredScenarioState(Scenario.STARTED);
            } else {
                stub.setRequiredScenarioState(scenarioName + "-" + count);
            }

            if (count < stubMappings.size()) {
                stub.setNewScenarioState(scenarioName + "-" + (count + 1));
            }

            count++;
        }

    }
}
