function  findEdge(id) {
    const edges = vue.graph.findAll('edge', edge => edge);
    for(let i=0; i<edges.length; ++i)
    {
        let edge = edges[i];
        if(edge.getModel().id == id) {
            return edge;
        }
    }
    return null;
}

function  findNode(id) {
    const nodes = vue.graph.findAll('node', node => node);
    for(let i=0; i<nodes.length; ++i)
    {
        let node = nodes[i];
        if(node.getModel().id == id) {
            return node;
        }
    }
    return null;
}

function findDataNode(id) {
    for(let i=0; i<vue.graphData.nodes.length; ++i)
    {
        let node = vue.graphData.nodes[i];
        if(node.id == id) {
            return node;
        }
    }
    return null;
}

function onSelectItem(item)
{
    if(item != null) {
        var model = item.getModel();
        if(model.id == 'shadowNode')
        {
            return;
        }
        if(model == null)
        {
            vue.selectType = '';
            return;
        }
        if(model.type != 'polyline')
        {
            vue.selectType = "step";
            vue.step = model.tag;
            if(vue.step  == null)
            {
                vue.step = {};
            }
            vue.step.x = parseInt(model.x);
            vue.step.y = parseInt(model.y);
            if(model.tag.stepType != 2) {
                vue.step.width = model.size;
            }
            else {
                vue.step.width = model.size[0];
            }

            vue.selectItemList.push(item);
        }
        else
        {
            vue.selectType = "stepRelation";
            vue.stepRelation = model.tag;
            if(vue.stepRelation == null)
            {
                vue.stepRelation = {};
            }
            var node = findNode(vue.stepRelation.fromStepId + "");
            if(node != null)
            {
                vue.stepRelation.fromStepName = node.getModel().label;
            }
            node = findNode(vue.stepRelation.toStepId + "");
            if(node != null)
            {
                vue.stepRelation.toStepName = node.getModel().label;
            }

            vue.stepRelation.fromStepAnchor = model.sourceAnchor;
            vue.stepRelation.toStepAnchor = model.targetAnchor;
        }
        vue.selectItem = item;
    }
    else
    {
        vue.selectType = '';
        vue.selectItemList = [];
    }
    console.log(vue.selectItemList);
}

function addNode(e) {
    axios.post(getApiUrl("workflowConfig", "newId"), {}).then(function (response) {
        if (isOK(response)) {
            var id = response.data.data.id + "";
            var node = {};
            node.x = e.x;
            node.y = e.y;
            node.type = 'rect';
            node.size = [80, 48];
            node.style = { fill: '#F2FBFE', stroke:'#7AD4F6', radius: 6, cursor: 'pointer' };
            node.anchorPoints = [
                [0.5, 0],
                [1, 0.5],
                [0.5, 1],
                [0, 0.5],
            ];
            node.id = id;

            var step = {};
            step.id = id;
            step.stepType = 2;
            node.tag = step;

            node = vue.graph.addItem('node', node);
            switchAction('select');
        }
    });
}

function onDelete() {
    const nodes = vue.graph.findAllByState('node', 'select');
    nodes.forEach((node) => {
        if(node.getModel().tag.stepType == 1) {
            parent.box.$message({showClose: true, message: "开始步骤不能删除", type: 'error'});
        }
        else if(node.getModel().tag.stepType == 3) {
            parent.box.$message({showClose: true, message: "结束步骤不能删除", type: 'error'});
        }
        else {
            vue.graph.remove(node);
        }
    });
    const edges = vue.graph.findAllByState('edge', 'select');
    edges.forEach((edge) => {
        vue.graph.remove(edge);
    });
    vue.graph.refresh();
}

function switchAction(action) {
    vue.action = action;
    vue.graph.setMode(action);
    if(vue.shadowNode != null)
    {
        vue.graph.remove(vue.shadowNode);
        vue.shadowNode = null;
    }
}

G6.registerBehavior('click-add-node', {
    getEvents() {
        return {
            'node:click': 'onNodeClick',
            mousemove: 'onMousemove',
        };
    },
    onNodeClick(e) {
        addNode(e);
    },
    onMousemove(e) {
        if(vue.shadowNode == null)
        {
            var node = {};
            node.id = 'shadowNode';
            node.x = e.x;
            node.y = e.y;
            node.type = 'rect';
            node.size = [80, 48];
            node.style = { fill: '#1890FF', stroke:'#1890FF', cursor: 'copy', radius: 6, fillOpacity: 0.1, lineDash:[4, 4]};
            vue.shadowNode = vue.graph.addItem('node', node);
        }
        vue.graph.updateItem( vue.shadowNode, {
            x: e.x,
            y: e.y
        });
    }

});

G6.registerBehavior('click-add-edge', {
    getEvents() {
        return {
            'canvas:click': 'onClick',
            'node:click': 'onNodeClick',
            mousemove: 'onMousemove',
            'edge:click': 'onEdgeClick'
        };
    },
    onClick(e) {
        this.edge = null;
        this.addingEdge = false;
        switchAction('select');
    },
    onNodeClick(e) {
        var that = this;
        const node = e.item;
        const point = {
            x: e.x,
            y: e.y
        };
        const model = node.getModel();
        if (this.addingEdge && this.edge) {
            vue.graph.updateItem(this.edge, {
                target: model.id
            });
            this.edge = null;
            this.addingEdge = false;
            switchAction('select');
            vue.graph.updateItem( node, {
                style: {cursor: 'pointer'}
            });
        }
        else {
            axios.post(getApiUrl("workflowConfig", "newId"), {}).then(function (response) {
                if (isOK(response)) {
                    var id = response.data.data.id + "";
                    var edge = {};
                    edge.id = id;
                    edge.type = 'polyline';
                    edge.color = '#AAA'
                    edge.style = { lineWidth: 1, lineAppendWidth: 6, endArrow: true  }
                    edge.source = model.id;
                    edge.target = point;
                    edge.tag = {id:id, fromStepId:edge.source};
                    that.edge = that.graph.addItem('edge', edge);
                    that.addingEdge = true;
                }
            });
        }
    },
    onMousemove(e) {
        const point = {
            x: e.x,
            y: e.y
        };
        if (this.addingEdge && this.edge) {
            vue.graph.updateItem(this.edge, {
                target: point
            });
        }
    },
    onEdgeClick(e) {
        const currentEdge = e.item;
        if (this.addingEdge && this.edge == currentEdge) {
            vue.graph.removeItem(this.edge);
            this.edge = null;
            this.addingEdge = false;
            switchAction('select');
        }
    }
});



function initGraph() {
    vue.graph.on('mousemove', (e) => {
    });

    vue.graph.on('mouseenter', (e) => {
    });

    vue.graph.on('mouseleave', (e) => {
    });


    vue.graph.on('node:mouseenter', (e) => {
        const nodeItem = e.item;
        vue.graph.setItemState(nodeItem, 'hover', true);

        if(vue.action == 'edge')
        {
            vue.graph.updateItem( nodeItem, {
                style: {cursor: 'crosshair'}
            });
        }
    });
    vue.graph.on('node:mouseleave', (e) => {
        const nodeItem = e.item;
        vue.graph.setItemState(nodeItem, 'hover', false);
        if(vue.action == 'edge')
        {
            vue.graph.updateItem( nodeItem, {
                style: {cursor: 'pointer'}
            });
        }
    });

    vue.graph.on('node:click', (e) => {
    });

    vue.graph.on('nodeselectchange', (e) => {
        const nodes = vue.graph.findAllByState('node', 'select');
        nodes.forEach((node) => {
            vue.graph.setItemState(node, 'select', false);
        });

        onSelectItem(null);

        e.selectedItems.nodes.forEach((node) => {
            vue.graph.setItemState(node, 'select', true);

            onSelectItem(node);
        });

        const edges = vue.graph.findAllByState('edge', 'select');
        edges.forEach((edge) => {
            vue.graph.setItemState(edge, 'select', false);
            vue.graph.setItemState(edge, 'unSelect', true);
        });
    });

    vue.graph.on('edge:click', (e) => {
        const nodes = vue.graph.findAllByState('node', 'select');
        nodes.forEach((node) => {
            vue.graph.setItemState(node, 'select', false);
        });
        onSelectItem(null);

        const edges = vue.graph.findAllByState('edge', 'select');
        edges.forEach((edge) => {
            vue.graph.setItemState(edge, 'select', false);
            vue.graph.setItemState(edge, 'unSelect', true);
        });
        const edgeItem = e.item;
        vue.graph.setItemState(edgeItem, 'select', true);

        onSelectItem(edgeItem);
    });

    vue.graph.on('click', (e)=>{
    });

    vue.graph.on('keydown', function(e){
        if(e.key == "Delete")
        {
            if(!vue.editingItem) {
                onDelete();
            }
        }
        else if(e.key == "ArrowLeft")
        {
            const nodes = vue.graph.findAllByState('node', 'select');
            nodes.forEach((node) => {
                vue.graph.update(node, {
                    x: node.getModel().x - 1
                });
            });
            vue.graph.refresh();
        }
        else if(e.key == "ArrowRight")
        {
            const nodes = vue.graph.findAllByState('node', 'select');
            nodes.forEach((node) => {
                vue.graph.update(node, {
                    x: node.getModel().x + 1
                });
            });
            vue.graph.refresh();
        }
        else if(e.key == "ArrowUp")
        {
            const nodes = vue.graph.findAllByState('node', 'select');
            nodes.forEach((node) => {
                vue.graph.update(node, {
                    y: node.getModel().y - 1
                });
            });
            vue.graph.refresh();
        }
        else if(e.key == "ArrowDown")
        {
            const nodes = vue.graph.findAllByState('node', 'select');
            nodes.forEach((node) => {
                vue.graph.update(node, {
                    y: node.getModel().y + 1
                });
            });
            vue.graph.refresh();
        }
    });
}

function initGraphData() {
    var postData = createJsonRequest({
        id: getUrlParameter("id")
    });
    axios.post(getApiUrl("workflowConfig", "get"), postData).then(function (response) {
        if (isOK(response)) {
            vue.entity = response.data.data.entity;
            vue.yesNo = response.data.data.yesNo;
            vue.roleList = response.data.data.roleList;
            vue.graphData.nodes = [];
            vue.graphData.edges = [];
            for (var i = 0; i < vue.entity.stepList.length; ++i) {
                var step = vue.entity.stepList[i];
                var node = {};
                node.id = step.id + "";
                node.x = step.x;
                node.y = step.y;
                node.tag = step;
                node.label = step.stepName;
                node.labelCfg = {
                    style: {fontSize: 15,},
                };

                if (step.stepType == 1) {
                    node.type = 'circle';
                    node.size = 60;
                    node.style = {fill: '#FFF3EA', stroke: '#F6BD16', cursor: 'pointer'};
                    node.anchorPoints = [
                        [0.5, 0],
                        [1, 0.5],
                        [0.5, 1],
                        [0, 0.5],
                    ];

                } else if (step.stepType == 2) {
                    node.type = 'rect';
                    node.size = [step.width, step.height];
                    node.style = {fill: '#F2FBFE', stroke: '#7AD4F6', radius: 6, cursor: 'pointer'};
                    node.anchorPoints = [
                        [0.5, 0],
                        [1, 0.5],
                        [0.5, 1],
                        [0, 0.5],
                    ];
                } else if (step.stepType == 3) {
                    node.type = 'circle';
                    node.size = 60;
                    node.style = {fill: '#F9F1FF', stroke: '#B37FEB', cursor: 'pointer'};
                    node.anchorPoints = [
                        [0.5, 0],
                        [1, 0.5],
                        [0.5, 1],
                        [0, 0.5],
                    ];
                }
                vue.graphData.nodes.push(node);
            }

            for (var i = 0; i < vue.entity.stepRelationList.length; ++i) {
                var stepRelation = vue.entity.stepRelationList[i];
                var edge = {};
                edge.id = stepRelation.id + "";
                edge.source = stepRelation.fromStepId + "";
                edge.sourceAnchor = stepRelation.fromStepAnchor;
                edge.target = stepRelation.toStepId + "";
                edge.targetAnchor = stepRelation.toStepAnchor;
                edge.tag = stepRelation;
                edge.type = 'polyline';
                edge.color = '#AAA'
                edge.style = {lineWidth: 1, lineAppendWidth: 6, endArrow: true, cursor: 'pointer'}
                vue.graphData.edges.push(edge);
            }

            var graph = new G6.Graph({
                container: 'canvas',
                width: 1000,
                height: window.innerHeight,
                modes: {
                    select: ['click-select', 'drag-node'],
                    node: ['click-add-node', 'click-select'],
                    edge: ['click-add-edge', 'click-select'],
                },
                nodeStateStyles: {
                    hover: {
                        fill: '#EFF4FF',
                    },
                    select: {
                        stroke: '#689BFC',
                        fill: '#EFF4FF',
                        lineWidth: 2,
                    },
                },
                edgeStateStyles: {
                    select: {
                        stroke: '#689BFC',
                        lineWidth: 2,
                        lineAppendWidth: 6
                    },
                    unSelect: {
                        stroke: '#AAA',
                        lineWidth: 1,
                        lineAppendWidth: 6
                    },
                }
            });
            graph.data(vue.graphData);
            graph.render();
            vue.graph = graph;
            initGraph();
            switchAction('select');
        }
    });

}
