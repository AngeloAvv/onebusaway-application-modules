/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.where.phone.templates;

import org.traditionalcake.probablecalls.agitemplates.AbstractAgiTemplate;
import org.traditionalcake.probablecalls.agitemplates.AgiTemplateId;

import com.opensymphony.xwork2.ActionContext;

@AgiTemplateId("/welcome")
public class WelcomeTemplate extends AbstractAgiTemplate {

    private static final long serialVersionUID = 1L;

    @Override
    public void buildTemplate(ActionContext context) {

        addAction("0", "/help", "message", Messages.INDEX_HELP);
        
        addAction("1", "/stop/index");
        addAction("2", "/find_your_stop");
        addAction("3", "/bookmarks/index");
        addAction("4", "/bookmarks/manage");
        addAction("5", "/most_recent");
        addAction("6", "/search/index");

        addAction("[#789*]", "/repeat");
        
        setNextAction("/index");

        addPause(1000);
        addMessage(Messages.WELCOME_ACTION);
    }
}
