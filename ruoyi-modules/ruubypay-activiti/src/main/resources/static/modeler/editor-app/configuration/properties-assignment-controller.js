/*
 * Activiti Modeler component part of the Activiti project
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/*
 * Assignment
 */
var KisBpmAssignmentCtrl = [ '$scope', '$modal', '$http', function($scope, $modal, $http) {

	var opts = {
	    template:  'editor-app/configuration/properties/assignment-popup.html?version=' + Date.now(),
	    scope: $scope
	};

	// Open the dialog
	$modal(opts);

}];

var KisBpmAssignmentPopupCtrl = [ '$scope', '$modal', function($scope, $modal) {

    // Put json representing assignment on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.assignment !== undefined
        && $scope.property.value.assignment !== null)
    {
        $scope.assignment = $scope.property.value.assignment;
    } else {
        $scope.assignment = {};
    }

    if ($scope.assignment.candidateUsers == undefined || $scope.assignment.candidateUsers.length == 0)
    {
    	$scope.assignment.candidateUsers = [{value: ''}];
    }

    // Click handler for + button after enum value
    var userValueIndex = 1;
    $scope.addCandidateUserValue = function(index) {
        $scope.assignment.candidateUsers.splice(index + 1, 0, {value: 'value ' + userValueIndex++});
    };

    // Click handler for - button after enum value
    $scope.removeCandidateUserValue = function(index) {
        $scope.assignment.candidateUsers.splice(index, 1);
    };

    if ($scope.assignment.candidateGroups == undefined || $scope.assignment.candidateGroups.length == 0)
    {
    	$scope.assignment.candidateGroups = [{value: ''}];
    }

    var groupValueIndex = 1;
    $scope.addCandidateGroupValue = function(index) {
        $scope.assignment.candidateGroups.splice(index + 1, 0, {value: 'value ' + groupValueIndex++});
    };

    // Click handler for - button after enum value
    $scope.removeCandidateGroupValue = function(index) {
        $scope.assignment.candidateGroups.splice(index, 1);
    };

    //Open the dialog to select users
    $scope.choseAssignment = function(flag) {

    	var opts = {
		    template:  'editor-app/configuration/properties/assignment-popup-popup.html?version=' + Date.now(),
		    scope: $scope
		};
		$scope.choseAssignmentFlag = flag;
		// Open the dialog
		$modal(opts);
    }

    //Open the dialog to select candidateGroups
    $scope.choseCandidateGroups = function(){
    	var opts = {
		    template:  'editor-app/configuration/properties/assignment-candidateGroup.html?version=' + Date.now(),
		    scope: $scope
		};
		// Open the dialog
		$modal(opts);
    }

    $scope.save = function() {

        $scope.property.value = {};
        handleAssignmentInput($scope);
        $scope.property.value.assignment = $scope.assignment;

        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    // Close button handler
    $scope.close = function() {
    	handleAssignmentInput($scope);
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };

    var handleAssignmentInput = function($scope) {
    	if ($scope.assignment.candidateUsers)
    	{
	    	var emptyUsers = true;
	    	var toRemoveIndexes = [];
	        for (var i = 0; i < $scope.assignment.candidateUsers.length; i++)
	        {
	        	if ($scope.assignment.candidateUsers[i].value != '')
	        	{
	        		emptyUsers = false;
	        	}
	        	else
	        	{
	        		toRemoveIndexes[toRemoveIndexes.length] = i;
	        	}
	        }

	        for (var i = 0; i < toRemoveIndexes.length; i++)
	        {
	        	$scope.assignment.candidateUsers.splice(toRemoveIndexes[i], 1);
	        }

	        if (emptyUsers)
	        {
	        	$scope.assignment.candidateUsers = undefined;
	        }
    	}

    	if ($scope.assignment.candidateGroups)
    	{
	        var emptyGroups = true;
	        var toRemoveIndexes = [];
	        for (var i = 0; i < $scope.assignment.candidateGroups.length; i++)
	        {
	        	if ($scope.assignment.candidateGroups[i].value != '')
	        	{
	        		emptyGroups = false;
	        	}
	        	else
	        	{
	        		toRemoveIndexes[toRemoveIndexes.length] = i;
	        	}
	        }

	        for (var i = 0; i < toRemoveIndexes.length; i++)
	        {
	        	$scope.assignment.candidateGroups.splice(toRemoveIndexes[i], 1);
	        }

	        if (emptyGroups)
	        {
	        	$scope.assignment.candidateGroups = undefined;
	        }
    	}
    };

    //?????????????????????????????????????????????????????????modal????????????????????????????????????angular.js?????????????????????????????????????????????
    $scope.$on('choseAssigneesStr', function(event,data){
	      $scope.assignment.candidateUsers[0].value = data;
	});
	$scope.$on('choseAssigneeStr', function(event,data){
	      $scope.assignment.assignee = data[0].code;
	});
	$scope.$on('choseCandidateGroupsStr', function(event,data){
		$scope.assignment.candidateGroups[0].value = data;
	});
}];

var KisBpmChoseAssignmentCtrl = ['$scope', '$http', function($scope, $http) {
	//????????????????????????????????????????????????????????????????????????
	var roles = [];
	var initId;
	$scope.getAllRoles = function (successCallback) {
	    $http({
	    	method: 'get',
	        headers: {'Accept': 'application/json',
	                  'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
	        url: '/dev-api/activiti/modeler/role/list'})

	        .success(function (data, status, headers, config) {
	        	var obj = data.rows;
	        	console.log("------------------" + obj)
	        	for (var i=0; i<obj.length; i++) {
	        		if (i==0) {
	        			initId = obj[i].roleKey + "";
	        			$scope.getAllAccountByRole(initId);
	        		}
	        		roles.push({ id: obj[i].roleKey, name: obj[i].roleName });
	        	}
	        	$scope.roles = roles;
	        })
	        .error(function (data, status, headers, config) {
	        });
	};
	$scope.getAllRoles(function(){});

	//???????????????????????????????????????????????????????????????????????????????????????
	 $scope.getAllAccountByRole = function(value) {
    	$http({
	    	method: 'get',
	        headers: {'Accept': 'application/json',
	                  'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
	        url: '/dev-api/activiti/modeler/user/listByRoleKey?roleKey=' + value})

	        .success(function (data, status, headers, config) {
	  		    if (data != null) {
					var accounts = [];
					var obj = data.rows;
					//????????????
					for (var i=0; i<obj.length; i++) {
	  		    		accounts.push({id: obj[i].userName, code: obj[i].userName, name : obj[i].nickName, index:i});
		        	}
	  		    	$scope.accounts=accounts;
	  		    }
	        })
	        .error(function (data, status, headers, config) {
	        });
    };

	// Close button handler
    $scope.close = function() {
    	$scope.$hide();
    };
    $scope.formData = {};
    $scope.candidateUser={};

    //Save Data
    $scope.save = function() {
    	if ($scope.choseAssignmentFlag == "assignee") {
    		// var choseAssignee = $scope.formData.assignee;
    		var choseAssignee = $scope.accounts;
    		$scope.$emit('choseAssigneeStr', choseAssignee);
    	} else if ($scope.choseAssignmentFlag == "assignees") {
    		var choseAssignees = $scope.accounts;
    		var choseAssigneesStr = "";
    		for (var i=0;i<choseAssignees.length; i++) {
    			if (choseAssignees[i].selected) {
    				choseAssigneesStr += choseAssignees[i].id + ",";
    			}
    		}
    		choseAssigneesStr = choseAssigneesStr.substring(0,choseAssigneesStr.length-1);
    		$scope.$emit('choseAssigneesStr', choseAssigneesStr);
    	}
        $scope.close();
    };
    $scope.selectAll = function($event) {
    	var checkbox = $event.target;
    	var choseAssignees = $scope.accounts;
    	for (var i=0;i<choseAssignees.length; i++) {
    		if (checkbox.checked) {
    			choseAssignees[i].selected = true;
        	} else {
        		choseAssignees[i].selected = false;
        	}
    	}
    	$scope.accounts = choseAssignees;
    }
}];

var KisBpmChoseCandidateGroupsCtrl = ['$scope', '$http', function($scope, $http) {

	var candidateGroups = [];
	$scope.getAllRoles = function (successCallback) {
	    $http({
	    	method: 'get',
	        headers: {'Accept': 'application/json',
	                  'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
	        url: '/dev-api/activiti/modeler/role/list'})

	        .success(function (data, status, headers, config) {
	        	var obj = data.rows;
	        	for (var i=0; i<obj.length; i++) {
					candidateGroups.push({ id: obj[i].roleKey, name: obj[i].roleName, description: obj[i].remark });
	        	}
	        	$scope.candidateGroups = candidateGroups;
	        })
	        .error(function (data, status, headers, config) {
	        });
	};
	$scope.getAllRoles(function() {
	});

	// Close button handler
    $scope.close = function() {
    	$scope.$hide();
    };

    $scope.save = function() {
    	var choseCandidateGroups = $scope.candidateGroups;
    	var choseCandidateGroupsStr = "";
    	for (var i=0;i<choseCandidateGroups.length; i++) {
    		if (choseCandidateGroups[i].selected) {
    			choseCandidateGroupsStr += choseCandidateGroups[i].id + ",";
			}
    	}
    	choseCandidateGroupsStr = choseCandidateGroupsStr.substring(0,choseCandidateGroupsStr.length-1);
		$scope.$emit('choseCandidateGroupsStr', choseCandidateGroupsStr);
		$scope.close();
    }

    $scope.selectAll = function($event) {
    	var checkbox = $event.target;
    	var candidateGroups = $scope.candidateGroups;
    	for (var i=0;i<candidateGroups.length; i++) {
    		if (checkbox.checked) {
    			candidateGroups[i].selected = true;
        	} else {
        		candidateGroups[i].selected = false;
        	}
    	}
    	$scope.candidateGroups = candidateGroups;
    }
}];
