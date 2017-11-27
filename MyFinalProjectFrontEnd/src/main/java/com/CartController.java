package com;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


import com.dao.CategoryDAO;
import com.dao.ProductDAO;
import com.dao.SupplierDAO;
import com.dao.UserDAO;
import com.daoimpl.CartDAOImpl;
import com.daoimpl.OrdersDAOImpl;
import com.model.Cart;
import com.model.Category;
import com.model.Orders;
import com.model.Product;
import com.model.User;

@Controller
public class CartController {

	@Autowired
	UserDAO userDAO;
	@Autowired
	CategoryDAO categoryDAO;
	@Autowired
	CartDAOImpl cartDaoImpl;
	@Autowired
	ProductDAO productDAO;
	@Autowired
	SupplierDAO supplierDAO;
	@Autowired
	OrdersDAOImpl ordersDaoImpl;

	
	@RequestMapping(value="/prodDetails/{catId}")
	public ModelAndView prodDet(@PathVariable("catId") int catId)
	{
		ModelAndView mv= new ModelAndView();
		Product p = productDAO.getProduct(catId);
		mv.addObject("product",p);
		mv.setViewName("prodDetails");
		return mv;
	}	
	
	
	@RequestMapping(value="/addToCart", method=RequestMethod.POST)
	public ModelAndView addToCart(HttpServletRequest req)
	{
		ModelAndView mv = new ModelAndView();
		Principal principal = req.getUserPrincipal();
		String userEmail=principal.getName();
		try
		{
			int pid=Integer.parseInt(req.getParameter("pId"));
			int price=Integer.parseInt(req.getParameter("pPrice"));
			String productName = req.getParameter("productName");
			String imgName=req.getParameter("imgname");
			int quantity = Integer.parseInt(req.getParameter("pStock"));			
			Cart cartexit = cartDaoImpl.getCartByID(pid,userEmail);
			
			if(cartexit == null)
			{
				Cart cm = new Cart();
				
				cm.setCartPrice(price);
				cm.setCartproductId(pid);
				cm.setCartQnty(quantity);
				cm.setCartImg(imgName);
				User u = userDAO.getUser(userEmail);
				cm.setCartUserDetails(u);
				cartDaoImpl.insert(cm);
			}
			else if(cartexit!=null)
			{
				Cart cm = new Cart();
				cm.setCartId(cartexit.getCartId());
				cm.setCartPrice(price);				
				cm.setCartproductId(pid);
				cm.setCartImg(imgName);
				cm.setCartProdName(productName);
				cm.setCartQnty(cartexit.getCartQnty()+quantity);
				User u = userDAO.getUser(userEmail);
				cm.setCartUserDetails(u);
				cartDaoImpl.Update(cm);
				
			}
		}catch(Exception ex)
		{
			mv.addObject("cartInfo",cartDaoImpl.findCartById(userEmail));
			mv.setViewName("cart");
			return mv;
		}
		mv.addObject("cartInfo",cartDaoImpl.findCartById(userEmail));
		mv.setViewName("cart");
		return mv;
		
		
	}
	
	
	
	@RequestMapping(value="/checkout",method=RequestMethod.GET)
	public ModelAndView chkproc(HttpServletRequest re)
	{
		ModelAndView mv = new ModelAndView();
		Principal pr = ((HttpServletRequest) re).getUserPrincipal();
		String userEmail = pr.getName();
		User u = userDAO.getUser(userEmail);
		List<Cart> cart = cartDaoImpl.findCartById(userEmail);
		mv.addObject("user",u);
		mv.addObject("cart", cart);
		return mv;
	}
	
	@RequestMapping(value="/invoiceProcess",method=RequestMethod.POST)
	public ModelAndView invoiceproc(HttpServletRequest req)
	{
		ModelAndView mv = new ModelAndView();
		Orders ord = new Orders();
		Double d =Double.parseDouble(req.getParameter("total"));
		String pmt = req.getParameter("payment");
		Principal principal =req.getUserPrincipal();
		String email =principal.getName();
		User usr = userDAO.getUser(email);
		
		ord.setUser(usr);
		ord.setTotal(d);
		ord.setPayment(pmt);
		ordersDaoImpl.addOrders(ord);
		mv.addObject("userDetails",usr);
		return mv;
		
		
	}
	
	@RequestMapping(value="/deletePCart/{cartId}")
	public ModelAndView deleteCartItem(@PathVariable("cartId")int cartId,HttpServletRequest req)
	{
		ModelAndView mv = new ModelAndView("checkout");
		Principal principal = req.getUserPrincipal();
		String userEmail = principal.getName();
		cartDaoImpl.deleteCart(cartId);
		mv.addObject("cartInfo", cartDaoImpl.findCartById(userEmail));
		mv.setViewName("cart");
		return mv;
	}

	@RequestMapping(value="/goToCart",method=RequestMethod.GET)
	public ModelAndView goToCart(HttpServletRequest request)
	{
		ModelAndView mv = new ModelAndView();
		Principal principal = request.getUserPrincipal();
		String userEmail = principal.getName();
		mv.addObject("cartInfo",cartDaoImpl.findCartById(userEmail));
		mv.setViewName("cart");
		return mv;
		
		
	}
	@RequestMapping(value="/ack")
	public String ack()
	{
		return "ackorders";
	}
}
