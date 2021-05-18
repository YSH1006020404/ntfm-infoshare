package cn.les.ntfm.infoshare.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author
 * @create 2020-10-29-上午11:07
 */
@Getter
@Setter
@ToString
public class InfoshareVO {
    private Long id;
    private String linkDescription;
    private String interactionMark;
    private String companyName;
    private Boolean stateFlag;
    private String sourceType;
    private Long sourceId;
    private Long xmlformatconfigId;
    private String tableName;
    private Boolean beatFag;
    private Boolean deduplicationFlag;
    private String destinations;
}
