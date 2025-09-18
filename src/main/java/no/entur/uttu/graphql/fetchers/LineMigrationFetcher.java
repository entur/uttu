/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.graphql.fetchers;

import static no.entur.uttu.graphql.GraphQLNames.FIELD_INPUT;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import no.entur.uttu.graphql.ArgumentWrapper;
import no.entur.uttu.service.LineMigrationService;
import no.entur.uttu.service.LineMigrationService.ConflictResolutionStrategy;
import no.entur.uttu.service.LineMigrationService.LineMigrationInput;
import no.entur.uttu.service.LineMigrationService.LineMigrationOptions;
import no.entur.uttu.service.LineMigrationService.LineMigrationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class LineMigrationFetcher implements DataFetcher<LineMigrationResult> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private LineMigrationService lineMigrationService;

  @Override
  @PreAuthorize("hasRole('ROLE_ROUTE_DATA_ADMIN')")
  public LineMigrationResult get(DataFetchingEnvironment env) {
    try {
      long startTime = System.currentTimeMillis();

      LineMigrationInput input = extractInput(env);
      LineMigrationResult result = lineMigrationService.migrateLine(input);

      long executionTime = System.currentTimeMillis() - startTime;
      if (result.getSummary() != null) {
        result.getSummary().setExecutionTimeMs(executionTime);
      }

      return result;
    } catch (SecurityException e) {
      logger.error("Authorization error in line migration: {}", e.getMessage());
      return createErrorResult("AUTHORIZATION_ERROR", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("Validation error in line migration: {}", e.getMessage());
      return createErrorResult("VALIDATION_ERROR", e.getMessage());
    } catch (Exception e) {
      logger.error("Unexpected error in line migration: {}", e.getMessage(), e);
      return createErrorResult("MIGRATION_ERROR", "Migration failed: " + e.getMessage());
    }
  }

  private LineMigrationInput extractInput(DataFetchingEnvironment env) {
    ArgumentWrapper inputWrapper = new ArgumentWrapper(env.getArgument(FIELD_INPUT));

    LineMigrationInput input = new LineMigrationInput();
    inputWrapper.apply("sourceLineId", input::setSourceLineId);
    inputWrapper.apply("targetProviderId", input::setTargetProviderId);
    inputWrapper.apply("targetNetworkId", input::setTargetNetworkId);

    // Extract options if provided
    inputWrapper.apply("options", this::extractOptions, input::setOptions);

    return input;
  }

  private LineMigrationOptions extractOptions(Map<String, Object> optionsMap) {
    if (optionsMap == null) {
      return new LineMigrationOptions();
    }

    ArgumentWrapper optionsWrapper = new ArgumentWrapper(optionsMap);
    LineMigrationOptions options = new LineMigrationOptions();

    optionsWrapper.apply(
      "conflictResolution",
      this::parseConflictResolution,
      options::setConflictResolution
    );
    optionsWrapper.apply("includeDayTypes", options::setIncludeDayTypes);
    optionsWrapper.apply("dryRun", options::setDryRun);

    return options;
  }

  private ConflictResolutionStrategy parseConflictResolution(String strategyStr) {
    if (strategyStr == null) {
      return ConflictResolutionStrategy.FAIL;
    }

    try {
      return ConflictResolutionStrategy.valueOf(strategyStr);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
        "Invalid conflict resolution strategy: " + strategyStr
      );
    }
  }

  private LineMigrationResult createErrorResult(String errorType, String errorMessage) {
    LineMigrationResult result = new LineMigrationResult();
    result.setSuccess(false);
    result.setErrorMessage(errorMessage);

    LineMigrationService.LineMigrationSummary summary =
      new LineMigrationService.LineMigrationSummary();
    summary.setEntitiesMigrated(0);
    summary.setWarningsCount(0);
    summary.setExecutionTimeMs(0);
    result.setSummary(summary);

    return result;
  }
}
