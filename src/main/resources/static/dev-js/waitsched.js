$(document).ready(function() {
	
	$.ajax({
		//默认值: true。如果需要发送同步请求，请将此选项设置为 false。注意，同步请求将锁住浏览器，用户其它操作必须等待请求完成才可以执行
		async : true,
		//默认值:"GET".请求方式 ("POST"或 "GET")，注意：其它 HTTP请求方法，如 PUT和 DELETE也可以使用，但仅部分浏览器支持
		type : 'POST',
		//默认值: "application/x-www-form-urlencoded"。发送信息至服务器时内容编码类型
		//默认值适合大多数情况。如果你明确指定$.ajax()的 content-type,那么它必定会发送给服务器（即使没有数据要发送）
		//contentType : "application/x-www-form-urlencoded",//application/json
		url : 'selectLabReport',
		data : $("#frmQuery").serialize(),
		//预期服务器返回的数据类型。如果不指定，jQuery将自动根据 HTTP包 MIME信息来智能判断
		dataType : 'json',
		beforeSend: function () {			
			document.getElementById("maskLayer").style.display="block";
        },
		success : function(data) {			
			
			$('#myTBody').bootstrapTable('load', data.response);
			
			$('#myTBody').bootstrapTable({
				
			    data: data.response,
				detailView:true,
			    detailFormatter:function(index, row, element){

			        var html = [];

			        $.each(row, function (key, value) {

			        	if((key==="唯一编号")||(key==="His唯一编号")||(key==="His门诊或住院")||(key==="所属部门")||(key==="工种")||(key==="工号")||(key==="婚否")||(key==="籍贯")||(key==="住址")||(key==="电话")||(key==="所属公司")||(key==="ifCompleted")||(key==="联机号")||(key==="流水号")||(key==="打印次数"))
			            	html.push('<p><b>' + key + ':</b> ' + value + '</p>');

			        });
			        return html.join('');
			    },
		        columns: [{
			        field: '姓名',
			        title: '姓名',
			        formatter: function formatter(value, row, index, field) {
			        	
			        	return "<a href='checkValue?unid="+row.唯一编号+"&ifCompleted="+row.ifCompleted+"' target='_blank'>" + value + "</a>";
			        }
			    }, {
			        field: '性别',
			        title: '性别'
			    }, {
			        field: '年龄',
			        title: '年龄'
			    }, {
			    	checkbox: true
			    }, {
			        field: '病历号',
			        title: '病历号'
			    }, {
			        field: '床号',
			        title: '床号'
			    }, {
			        field: '送检科室',
			        title: '送检科室'
			    }, {
			        field: '送检医生',
			        title: '送检医生'
			    }, {
			        field: '检查日期',
			        title: '检查日期'
			    }, {
			        field: '申请日期',
			        title: '申请日期'
			    }, {
			        field: '审核者',
			        title: '审核者'
			    }, {
			        field: '工作组',
			        title: '工作组'
			    }, {
			        field: '操作者',
			        title: '操作者'
			    }, {
			        field: '优先级别',
			        title: '优先级别'
			    }, {
			        field: '样本类型',
			        title: '样本类型'
			    }, {
			        field: '临床诊断',
			        title: '临床诊断'
			    }, {
			        field: '样本情况',
			        title: '样本情况'
			    }, {
			        field: '备注',
			        title: '备注'
			    }, {
			        field: '审核时间',
			        title: '审核时间'
			    }, {
			    	width: '0px',//todo-list,不起作用
			        field: '唯一编号',
			        //title: '唯一编号',//为减小宽度而注释
			        class: 'unid'//用于打印
			    }, {
			    	width: '0px',//todo-list,不起作用
			        field: 'ifCompleted',
			        //title: 'ifCompleted',//为减小宽度而注释
				    class: 'ifCompleted'//用于打印
			    }, {
			    	width: '0px',//todo-list,不起作用
			        field: '打印次数',
			        //title: '打印次数',//为减小宽度而注释
				    class: 'printtimes'//用于打印
			    }]
			});
						
			document.getElementById("maskLayer").style.display="none";
		},
		error : function(xhr, textStatus, errorThrown) {
			
			document.getElementById("maskLayer").style.display="none";
			console.log("ajax请求失败,请求:selectLabReport,状态码:"+xhr.status +",状态说明:"+ textStatus+",xhr readyState:"+xhr.readyState);
		}
	});
		
});