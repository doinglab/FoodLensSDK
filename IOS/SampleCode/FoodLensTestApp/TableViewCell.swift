//
//  TableViewCell.swift
//  FoodLensTestApp
//
//  Created by HwangChun Lee on 02/04/2019.
//  Copyright © 2019 DoingLab Inc. All rights reserved.
//

import UIKit

class TableViewCell: UITableViewCell {
    @IBOutlet weak var foodImage: UIImageView!
    @IBOutlet weak var foodName: UILabel!
    @IBOutlet weak var foodUnit: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
