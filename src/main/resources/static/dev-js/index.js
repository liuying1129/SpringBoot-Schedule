$(document).ready(function() {
	
	$.ajax({
		//默认值: true。如果需要发送同步请求，请将此选项设置为 false。注意，同步请求将锁住浏览器，用户其它操作必须等待请求完成才可以执行
		async : true,
		//默认值:"GET".请求方式 ("POST"或 "GET")，注意：其它 HTTP请求方法，如 PUT和 DELETE也可以使用，但仅部分浏览器支持
		type : 'POST',
		//默认值: "application/x-www-form-urlencoded"。发送信息至服务器时内容编码类型
		//默认值适合大多数情况。如果你明确指定$.ajax()的 content-type,那么它必定会发送给服务器（即使没有数据要发送）
		//contentType : "application/x-www-form-urlencoded",//application/json
		url : 'queryAllJob',
		//预期服务器返回的数据类型。如果不指定，jQuery将自动根据 HTTP包 MIME信息来智能判断
		dataType : 'json',
		success : function(data) {			
			
			$('#allJob').bootstrapTable('load', data.response);
			
			$('#allJob').bootstrapTable({
				
			    data: data.response,
		        columns: [{
					title: '序号',
					formatter: function (value, row, index) {
						return index+1;
					}
	            },{
			        field: 'jobName',
			        title: 'Job名称',
			    }, {
			        field: 'jobDataMapTaskConfigCode',
			        title: '任务编号(JobKey)'
			    }, {
			        field: 'jobDataMapTaskContentCode',
			        title: 'TaskContentCode'
			    }, {
			        field: 'jobTriggerState',
			        title: 'Trigger状态'
			    }, {
			        field: 'jobTriggerCron',
			        title: 'Cron'
			    }, {
			        field: 'triggerPreviousFireTime',
			        title: '上次执行时间'
			    }, {
			        field: 'triggerNextFireTime',
			        title: '下次执行时间'
			    }, {
			        title: 'Trigger',
					formatter: function (value, row, index) {
						return row.jobTrigger.group+"<br />"+row.jobTrigger.name;
					}
			    }, {
			        field: 'triggerStartTime',
			        title: '触发器启动时间'
			    }]
			});						
		},
		error : function(xhr, textStatus, errorThrown) {
			
			console.log("ajax请求失败,请求:queryAllJob,状态码:"+xhr.status +",状态说明:"+ textStatus+",xhr readyState:"+xhr.readyState);
		}
	});
	
	$.ajax({
		//默认值: true。如果需要发送同步请求，请将此选项设置为 false。注意，同步请求将锁住浏览器，用户其它操作必须等待请求完成才可以执行
		async : true,
		//默认值:"GET".请求方式 ("POST"或 "GET")，注意：其它 HTTP请求方法，如 PUT和 DELETE也可以使用，但仅部分浏览器支持
		type : 'POST',
		//默认值: "application/x-www-form-urlencoded"。发送信息至服务器时内容编码类型
		//默认值适合大多数情况。如果你明确指定$.ajax()的 content-type,那么它必定会发送给服务器（即使没有数据要发送）
		//contentType : "application/x-www-form-urlencoded",//application/json
		url : 'queryRunningJob',
		//预期服务器返回的数据类型。如果不指定，jQuery将自动根据 HTTP包 MIME信息来智能判断
		dataType : 'json',
		success : function(data) {			
			
			$('#runningJob').bootstrapTable('load', data.response);
			
			$('#runningJob').bootstrapTable({
				
			    data: data.response,
		        columns: [{
			        field: 'jobName',
			        title: 'Job名称',
			    }, {
			        field: 'jobDataMapTaskConfigCode',
			        title: '任务编号(JobKey)'
			    }, {
			        field: 'jobDataMapTaskContentCode',
			        title: 'TaskContentCode'
			    }, {
			        field: 'jobDataMapTaskDesc',
			        title: '任务描述'
			    }, {
			        field: 'jobTriggerState',
			        title: 'Trigger状态'
			    }, {
			        field: 'jobTriggerCron',
			        title: 'Cron'
			    }, {
			        field: 'triggerPreviousFireTime',
			        title: '上次执行时间'
			    }, {
			        field: 'triggerNextFireTime',
			        title: '下次执行时间'
			    }, {
			        field: 'executingJobFireTime',
			        title: '实际执行时间'
			    }, {
			        field: 'executingJobRunTime',
			        title: '运行时长(秒)'
			    }]
			});						
		},
		error : function(xhr, textStatus, errorThrown) {
			
			console.log("ajax请求失败,请求:queryRunningJob,状态码:"+xhr.status +",状态说明:"+ textStatus+",xhr readyState:"+xhr.readyState);
		}
	});	
		
});