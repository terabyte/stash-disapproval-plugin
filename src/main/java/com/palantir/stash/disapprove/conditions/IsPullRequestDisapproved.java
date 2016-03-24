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
package com.palantir.stash.disapprove.conditions;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.palantir.stash.disapprove.logger.PluginLoggerFactory;
import com.palantir.stash.disapprove.persistence.PersistenceManager;
import com.palantir.stash.disapprove.persistence.PullRequestDisapproval;
import java.sql.SQLException;
import java.util.Map;
import org.slf4j.Logger;

/**
 * A condition which checks that the pull request is disapproved (or not). <code>
 * <pre>
 * &lt;condition class="com.palantir.stash.disapprove.conditions.IsPullRequestDisapproved"/>
 * </pre>
 * </code> Add <code>invert="true"</code> to reverse the result.
 * 
 * @author cmyers
 */
public class IsPullRequestDisapproved implements Condition {

    private final PersistenceManager pm;
    private final Logger log;

    public IsPullRequestDisapproved(PersistenceManager pm, PluginLoggerFactory plf) {
        this.pm = pm;
        this.log = plf.getLogger(IsPullRequestDisapproved.class.toString());
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException {
        // nothing to do here, no params are needed
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        final PullRequest pr = (PullRequest) context.get("pullRequest");
        PullRequestDisapproval prd;
        try {
            prd = pm.getPullRequestDisapproval(pr);
        } catch (SQLException e) {
            log.error("Unable to get disapproval metadata", e);
            // err on the side of not showing the buttons
            return false;
        }
        return prd.isDisapproved();
    }

}
