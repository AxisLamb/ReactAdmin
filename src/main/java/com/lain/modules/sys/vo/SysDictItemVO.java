// SysDictItemVO.java
package com.lain.modules.sys.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "数据字典项VO")
public class SysDictItemVO {
    @Schema(description = "字典项ID")
    private Long itemId;

    @Schema(description = "字典ID")
    private Long dictId;

    @Schema(description = "字典项标签")
    private String itemLabel;

    @Schema(description = "字典项值")
    private String itemValue;

    @Schema(description = "状态（0禁用 1正常）")
    private Integer status = 1;

    @Schema(description = "显示顺序")
    private Integer orderNum = 0;

    @Schema(description = "备注")
    private String remark;

}
