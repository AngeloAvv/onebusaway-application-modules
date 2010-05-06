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
package edu.washington.cs.rse.transit.phone.templates.stops;

import org.traditionalcake.probablecalls.agitemplates.AbstractAgiTemplate;
import org.traditionalcake.probablecalls.agitemplates.AgiTemplateId;

import com.opensymphony.xwork2.ActionContext;

import edu.washington.cs.rse.transit.phone.templates.Messages;

@AgiTemplateId("/stop/index")
public class IndexTemplate extends AbstractAgiTemplate {

    private static final long serialVersionUID = 1L;

    @Override
    public void buildTemplate(ActionContext context) {

        addMessage(Messages.STOP_INDEX_ACTION);
        addActionWithParameterFromMatch("([1-9][0-9]*)#", "/stop/byId", "stopId", 1);

        addAction("(#|0|[1-9].*\\*)", "/repeat");

        addMessage(Messages.HOW_TO_GO_BACK);
        addAction("\\*", "/back");

        addMessage(Messages.TO_REPEAT);
    }
}
