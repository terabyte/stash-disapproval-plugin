// Copyright 2014 Palantir Technologies
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.palantir.stash.disapprove.mergecheck;

import com.atlassian.bitbucket.event.pull.PullRequestEvent;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.event.api.EventListener;
import com.palantir.stash.disapprove.logger.PluginLoggerFactory;
import com.palantir.stash.disapprove.persistence.PersistenceManager;
import org.slf4j.Logger;

/**
 * This class ensures that the metadata for a PullRequestDisapproval is created at the time a pull request is created,
 * because it cannot be auto-vivified during a merge check because merge checks are wrapped in a read-only DB
 * transaction *angry face*
 * 
 * @author cmyers
 * 
 */
public class PullRequestMetadataDeRuiner {

    private final PersistenceManager cpm;
    private final Logger log;

    public PullRequestMetadataDeRuiner(PersistenceManager cpm, PluginLoggerFactory lf) {
        this.cpm = cpm;
        this.log = lf.getLoggerForThis(this);
    }

    /**
     * Ensures all repositories with pull requests have a configuration, and all pull requests have a configuration.
     * 
     * @param pre
     */
    @EventListener
    public void listenToPullRequestEvents(PullRequestEvent pre) {
        PullRequest pr = pre.getPullRequest();
        Repository repo = pr.getToRef().getRepository();
        // XXX: migration: this can't be null anymore, so what does it mean not to be "materialized"? maybe this is impossible now?
        if (pr.getId() == 0) {
            log.debug("Got NULL PR_ID, ignoring until PR is fully materialized");
            return;
        }
        try {
            log.trace("Ensuring DisapprovalConfiguration exists for REPO " + repo.getId());
            cpm.getDisapprovalConfiguration(repo);
            log.trace("Ensuring PullRequestDisapproval exists for PR " + pr.getId());
            cpm.getPullRequestDisapproval(pr);
        } catch (Exception e) {
            log.error("Undeclared exception: ", e);
            return;
        }
    }
}
