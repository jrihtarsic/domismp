import {MatTreeNestedDataSource} from "@angular/material/tree";
import {Injectable, Input} from "@angular/core";
import {SecurityService} from "../../security/security.service";
import {SecurityEventService} from "../../security/security-event.service";
import {SmpConstants} from "../../smp.constants";
import {HttpClient} from "@angular/common/http";
import {User} from "../../security/user.model";
import {NavigationEnd, Router} from "@angular/router";
import {Observable, Subject} from "rxjs";
import {filter, map} from "rxjs/operators";
import {LocalStorageService} from "../../common/services/local-storage.service";

/**
 * The smp navigation tree
 */

let PUBLIC_NAVIGATION_TREE: NavigationNode = {
  code: "home",
  i18n: "navigation.label.home",
  icon: "home",
  routerLink: "",
  children: [
    {
      code: "search-tools",
      i18n: "navigation.label.search",
      icon: "search",
      tooltipI18n: "navigation.tooltip.search.tools",
      routerLink: "public",
      children: [
        {
          code: "search-resources",
          i18n: "navigation.label.search.resources",
          icon: "find_in_page",
          tooltipI18n: "navigation.tooltip.search.resources",
          routerLink: "search-resources",
        }
      ]
    }
  ]
};


/**
 * Navigation  data with nested structure.
 * Each node has a name and an optional list of children.
 */
export interface NavigationNode {
  code: string;
  i18n: string;
  icon?: string;
  tooltipI18n?: string;
  routerLink?: string;
  children?: NavigationNode[];
  clickable?: boolean;
  selected?: boolean;
  transient?: boolean; // if true then node must be ignored
}

@Injectable()
export class NavigationService extends MatTreeNestedDataSource<NavigationNode> {

  private sub = this.router.events
    .pipe(
      filter(event => event instanceof NavigationEnd),
      map(event => event as NavigationEnd),  // appease typescript
    )
    .subscribe(
      event => {
        console.log('NavigationService: ' + event.url)
        let path: string[] = event.url.split('/');
        this.setNavigationTreeByPath(path, this.rootNode);
      }
    );


  private selectedPathSubject = new Subject<NavigationNode[]>();
  selected: NavigationNode;
  previousSelected: NavigationNode;
  _selectedPath: NavigationNode[];

  private rootNode: NavigationNode = PUBLIC_NAVIGATION_TREE;

  constructor(protected securityService: SecurityService,
              protected securityEventService: SecurityEventService,
              protected http: HttpClient,
              protected router: Router,
              protected localStorageService: LocalStorageService) {
    super();
    // set  tree data.
    this.refreshNavigationTree();
    // refresh navigation tree on login/logout even types
    securityEventService.onLoginSuccessEvent().subscribe(value => {
        this.refreshNavigationTree();
      }
    );
    securityEventService.onLogoutSuccessEvent().subscribe(value => {
        this.reset();
      }
    );
    securityEventService.onLogoutErrorEvent().subscribe(value => {
        this.reset();
      }
    );
  }

  @Input() set selectedPath(path: NavigationNode[]) {
    this.localStorageService.storeNavigationPath(path);
    this._selectedPath = path;
  }

  get selectedPath(): NavigationNode[] {
    if (!this._selectedPath || this._selectedPath?.length == 0) {
      this._selectedPath = this.localStorageService.getNavigationPath();
    }
    return this._selectedPath;
  }

  ngOnDestroy() {
    console.log('>> STOP listening to route events ');
    this.sub.unsubscribe();
  }

  select(node: NavigationNode) {
    let targetNode = this.findLeaf(node);
    if (targetNode === this.selected) {
      return
    }
    if (!!targetNode) {
      if (this.selected) {
        // unselect current value
        this.selected.selected = false;
      }
      this.previousSelected = this.selected;
      this.selected = targetNode
      this.selected.selected = true;
      this.selectedPath = this.findPathForNode(this.selected, this.rootNode);
      this.markNodesAsClickable(this.selectedPath);
      this.selectedPathSubject.next(this.selectedPath);

      // navigate to selected path
      let navigationPath: string[] = this.getNavigationPath(this.selectedPath);
      this.router.navigate(navigationPath);
    } else {
      this.selectedPathSubject.next(null);
    }
  }

  private markNodesAsClickable(selectedPath: NavigationNode[]) {
    if (selectedPath) {
      // reset all  nodes (maybe some previously marked as non-clickable)
      selectedPath.forEach(value => value.clickable = true);

      if (selectedPath.length) {
        let leafIndex = selectedPath.length - 1;

        // mark the selected leaf as non-clickable
        selectedPath[leafIndex].clickable = false;

        // mark the parent of the first leaf in a menu as non-clickable
        let parent = this.findParent(selectedPath[leafIndex]);
        let grandParent = this.findParent(parent);
        if (parent && parent.children && parent.children[0] == selectedPath[leafIndex] && grandParent == this.rootNode) {
          parent.clickable = false;
        }

        // mark the root parent as non-clickable when selecting the very first leaf in a three level tree
        let userRootLeaf = this.getDeepestLeaf(this.rootNode);
        if (userRootLeaf == selectedPath[leafIndex] && selectedPath.length == 3) {
          this.rootNode.clickable = false;
        }
      }
    }
  }

  selectPreviousNode() {
    this.select(this.previousSelected)
  }

  public reset() {
    this.rootNode = PUBLIC_NAVIGATION_TREE;
    this.data = this.rootNode.children;
    this.select(this.rootNode)
  }

  protected getNavigationPath(path: NavigationNode[]): string [] {
    return path.map(node => node.routerLink);
  }

  protected findLeaf(targetNode: NavigationNode): NavigationNode {
    if (this.noTargetChildren(targetNode)) {
      return targetNode;
    }

    let newTargetNode = targetNode.children[0]
    return this.findLeaf(newTargetNode);
  }

  protected noTargetChildren(targetNode: NavigationNode): boolean {
    return this.findSiblings(targetNode).length == 0;
  }

  protected findSiblings(node: NavigationNode): NavigationNode[] {
    if (!node || !node.children || node.children.length == 0) {
      return [];
    }

    return node.children.filter(node => !node.transient);
  }

  protected findParent(node: NavigationNode): NavigationNode {
    let path = this.findPathForNode(node, this.rootNode);
    if (path) {
      let parentIndex = path.indexOf(node) - 1;
      return path[parentIndex];
    }
    return null;
  }

  private getDeepestLeaf(currentNode: NavigationNode): NavigationNode {
    if (this.noTargetChildren(currentNode)) {
      return currentNode;
    }
    return this.getDeepestLeaf(currentNode.children[0]);
  }

  /**
   * Find vertical path as example [root, parent, target node] for the target node
   * @param targetNode the target node
   * @param parentNode - the root of the tree to start search
   */
  protected findPathForNode(targetNode: NavigationNode, parentNode: NavigationNode): NavigationNode[] {
    if (parentNode.code === targetNode.code) {
      return [parentNode];
    }
    if (!parentNode.children) {
      return null;
    }

    let node: NavigationNode =  this.findNodeByCode(targetNode.code, parentNode);
    if (node) {
      // got target return initial array
      return [parentNode, node];
    }

    for (const child of parentNode.children) {
      let result = this.findPathForNode(targetNode, child);
      if (result) {
        return [parentNode, ...result];
      }
    }
    return null;
  }

  protected findNodeByCode(nodeCode: string, parentNode: NavigationNode): NavigationNode {
    if (!parentNode.children) {
      return null;
    }
    return parentNode.children.find(node => node.routerLink == nodeCode);
  }

  /**
   * Refresh navigation tree for user
   */
  public refreshNavigationTree() {
    this.securityService.isAuthenticated(false).subscribe((isAuthenticated: boolean) => {
      if (isAuthenticated) {
        const currentUser: User = this.securityService.getCurrentUser();
        // get navigation for user
        let navigationObserver = this.http.get<NavigationNode>(SmpConstants.REST_PUBLIC_USER_NAVIGATION_TREE.replace(SmpConstants.PATH_PARAM_ENC_USER_ID, currentUser.userId));

        navigationObserver.subscribe({
          next: (userRootNode: NavigationNode) => {
            this.setNavigationTree(userRootNode)
          }, error: (error: any) => {
            // check if unauthorized
            // just console try latter
            console.log("Error occurred while retrieving the navigation model for the user[" + error + "]");
          }
        });
      }
    });
  };

  setNavigationTree(userRootNode: NavigationNode) {
    // find the node by the navigation
    let path: string[] = this.router.url.split('/');
    this.setNavigationTreeByPath(path, userRootNode)
  }

  setNavigationTreeByPath(path: string[], userRootNode: NavigationNode) {
    this.rootNode = userRootNode;
    this.data = this.rootNode?.children;
    this.selectStartNode(path, userRootNode);
  }

  private selectStartNode(path: string[], userRootNode: NavigationNode) {
    let startNode = userRootNode;
    for (let index in path) {
      let pathSegment = path[index];
      // the first node is empty - skip all empty nodes
      if (!!pathSegment) {
        startNode = this.findNodeByCode(path[index], startNode);
        if (startNode == null) {
          break;
        }
      }
    }
    this.select(startNode);
  }

  getSelectedPathObservable(): Observable<NavigationNode[]> {
    return this.selectedPathSubject.asObservable();
  }

  /** Add node as child of parent */
  public add(node: NavigationNode, parent: NavigationNode) {
    // add root node
    //const rootNode = {code: "home", name: "Home", icon: "home", children: this.data};
    this._add(node, parent, this.rootNode);
    this.data = this.rootNode.children;
  }

  /** Remove node from tree */
  public remove(node: NavigationNode) {
    const newTreeData: NavigationNode = {
      code: "home",
      i18n: "navigation.label.home",
      icon: "home",
      children: this.data
    };
    this._remove(node, newTreeData);
    this.data = newTreeData.children;
  }

  /*
   * For immutable update patterns, have a look at:
   * https://redux.js.org/recipes/structuring-reducers/immutable-update-patterns/
   */

  protected _add(newNode: NavigationNode, parent: NavigationNode, tree: NavigationNode) {
    if (tree === parent) {
      console.log(
        `replacing children array of '${parent.i18n}', adding ${newNode.i18n}`
      );
      tree.children = [...tree.children!, newNode];
      return true;
    }
    if (!tree.children) {
      console.log(`reached leaf node '${tree.i18n}', backing out`);
      return false;
    }
    return this.update(tree, this._add.bind(this, newNode, parent));
  }

  _remove(node: NavigationNode, tree: NavigationNode): boolean {
    if (!tree.children) {
      return false;
    }
    const i = tree.children.indexOf(node);
    if (i > -1) {
      tree.children = [
        ...tree.children.slice(0, i),
        ...tree.children.slice(i + 1)
      ];
      console.log(`found ${node.i18n}, removing it from`, tree);
      return true;
    }
    return this.update(tree, this._remove.bind(this, node));
  }

  protected update(tree: NavigationNode, predicate: (n: NavigationNode) => boolean) {
    let updatedTree: NavigationNode, updatedIndex: number;

    tree.children!.find((node, i) => {
      if (predicate(node)) {
        console.log(`creating new node for '${node.i18n}'`);
        updatedTree = {...node};
        updatedIndex = i;
        return true;
      }
      return false;
    });

    if (updatedTree!) {
      console.log(`replacing node '${tree.children![updatedIndex!].i18n}'`);
      tree.children![updatedIndex!] = updatedTree!;
      return true;
    }
    return false;
  }

  public navigateToLogin(): void {
    this.securityService.clearLocalStorage()
    this.reset();
    let node: NavigationNode = this.createLoginNode();
    this.rootNode.children.push(node);
    this.select(node);
  }

  public navigateToHome(): void {
    this.select(this.rootNode);
  }

  public navigateUp(): void {
    let currentPath = this.selectedPath;
    currentPath?.pop();
    this._selectedPath = currentPath;
    if (currentPath?.length > 0) {
      this.select(currentPath[currentPath?.length - 1]);
    }
  }

  public navigateToUserDetails(): void {
    this.setNavigationTreeByPath(['user-settings', 'user-profile'], this.rootNode)
  }

  public createLoginNode(): NavigationNode {
    return {
      code: "login",
      icon: "login",
      i18n: "navigation.label.login",
      routerLink: "login",
      clickable: true,
      selected: true,
      tooltipI18n: "",
      transient: true
    }
  }

}
