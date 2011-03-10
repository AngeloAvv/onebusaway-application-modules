package org.onebusaway.transit_data.model.service_alerts;

import java.io.Serializable;
import java.util.List;

public class SituationBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private long creationTime;

  private TimeRangeBean publicationWindow;

  private String miscellaneousReason;

  private String personnelReason;

  private String equipmentReason;

  private String environmentReason;

  private String undefinedReason;

  private NaturalLanguageStringBean summary;

  private NaturalLanguageStringBean description;

  private NaturalLanguageStringBean detail;

  private NaturalLanguageStringBean advice;

  private NaturalLanguageStringBean internal;

  private SituationAffectsBean affects;

  private List<SituationConsequenceBean> consequences;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(long creationTime) {
    this.creationTime = creationTime;
  }

  public TimeRangeBean getPublicationWindow() {
    return publicationWindow;
  }

  public void setPublicationWindow(TimeRangeBean publicationWindow) {
    this.publicationWindow = publicationWindow;
  }

  public String getMiscellaneousReason() {
    return miscellaneousReason;
  }

  public void setMiscellaneousReason(String miscellaneousReason) {
    this.miscellaneousReason = miscellaneousReason;
  }

  public String getPersonnelReason() {
    return personnelReason;
  }

  public void setPersonnelReason(String personnelReason) {
    this.personnelReason = personnelReason;
  }

  public String getEquipmentReason() {
    return equipmentReason;
  }

  public void setEquipmentReason(String equipmentReason) {
    this.equipmentReason = equipmentReason;
  }

  public String getEnvironmentReason() {
    return environmentReason;
  }

  public void setEnvironmentReason(String environmentReason) {
    this.environmentReason = environmentReason;
  }

  public String getUndefinedReason() {
    return undefinedReason;
  }

  public void setUndefinedReason(String undefinedReason) {
    this.undefinedReason = undefinedReason;
  }

  public NaturalLanguageStringBean getSummary() {
    return summary;
  }

  public void setSummary(NaturalLanguageStringBean summary) {
    this.summary = summary;
  }

  public NaturalLanguageStringBean getDescription() {
    return description;
  }

  public void setDescription(NaturalLanguageStringBean description) {
    this.description = description;
  }

  public NaturalLanguageStringBean getDetail() {
    return detail;
  }

  public void setDetail(NaturalLanguageStringBean detail) {
    this.detail = detail;
  }

  public NaturalLanguageStringBean getAdvice() {
    return advice;
  }

  public void setAdvice(NaturalLanguageStringBean advice) {
    this.advice = advice;
  }

  public NaturalLanguageStringBean getInternal() {
    return internal;
  }

  public void setInternal(NaturalLanguageStringBean internal) {
    this.internal = internal;
  }

  public SituationAffectsBean getAffects() {
    return affects;
  }

  public void setAffects(SituationAffectsBean affects) {
    this.affects = affects;
  }

  public List<SituationConsequenceBean> getConsequences() {
    return consequences;
  }

  public void setConsequences(List<SituationConsequenceBean> consequences) {
    this.consequences = consequences;
  }
}
